package de.scenechain.auth;

import de.scenechain.config.SceneChainProperties;
import de.scenechain.crypto.CredentialCrypto;
import de.scenechain.protocol.Protocol;
import de.scenechain.scene.SceneService;
import de.scenechain.user.AccountRepository;
import de.scenechain.user.HotspotAggregateRepository;
import de.scenechain.study.StudyEventRepository;
import de.scenechain.study.ReleaseGateRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    public record SceneDto(int id, int version, String family, String title, String asset, String thumbnail,
                           String license, List<Integer> eligibleCells) {}
    public record StartResponse(String csrfToken, String handle, List<SceneDto> scenes) {}
    public record ConsentRequest(boolean informed, boolean adult, boolean voluntary, boolean researchMetrics,
                                 boolean deletionRights, boolean comprehensionPassed) {}
    public record StageInput(@jakarta.validation.constraints.Min(1) int sceneId,
                             @jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(383) int cellId,
                             @jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(3) int actionId) {}
    public record ConfirmationRequest(@Size(min = 5, max = 5) List<@Valid StageInput> stages) {}
    public record ConfirmationResponse(int matchingConfirmations, int requiredConfirmations) {}
    public record CompleteRequest(@Size(min = 5, max = 5) List<@Valid StageInput> stages,
                                  @NotBlank @Size(min = 15, max = 128) String password,
                                  @jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(3600000) int totalMs,
                                  @Size(max = 5) List<@jakarta.validation.constraints.Min(0) Integer> stageMs) {}
    public record CompleteResponse(String handle) {}
    private static final java.util.Set<String> BLOCKED = java.util.Set.of(
        "password", "password123", "password123456", "qwertyuiop", "qwerty123456",
        "123456789012345", "1234567890abcdef", "letmein123456789", "iloveyou123456789",
        "adminadminadmin", "welcome123456789", "correcthorsebatterystaple", "scenechain",
        "scenechainpassword", "changemechangeme", "footballfootball", "monkeymonkeymonkey",
        "dragon123456789", "sunshine123456789", "princess123456789", "trustnoone123456");
    private final SceneService scenes;
    private final CredentialCrypto crypto;
    private final AttemptStore attempts;
    private final AccountRepository accounts;
    private final HotspotAggregateRepository hotspots;
    private final Cookies cookies;
    private final RateLimiter limiter;
    private final SceneChainProperties properties;
    private final StudyEventRepository studyEvents;
    private final boolean recruitmentEnabled;
    private final SessionStore sessions;
    private final ReleaseGateRepository releaseGate;

    public EnrollmentController(SceneService scenes, CredentialCrypto crypto, AttemptStore attempts,
                                AccountRepository accounts, HotspotAggregateRepository hotspots,
                                Cookies cookies, RateLimiter limiter,
                                SceneChainProperties properties, StudyEventRepository studyEvents,
                                SessionStore sessions, ReleaseGateRepository releaseGate,
                                @Value("${scenechain.recruitment-enabled:false}") boolean recruitmentEnabled) {
        this.scenes = scenes;
        this.crypto = crypto;
        this.attempts = attempts;
        this.accounts = accounts;
        this.hotspots = hotspots;
        this.cookies = cookies;
        this.limiter = limiter;
        this.properties = properties;
        this.studyEvents = studyEvents;
        this.sessions = sessions;
        this.releaseGate = releaseGate;
        this.recruitmentEnabled = recruitmentEnabled;
    }

    @PostMapping("/start")
    public StartResponse start(@Valid @RequestBody ConsentRequest consent, HttpServletRequest request, HttpServletResponse response) {
        if (!recruitmentEnabled || !"formal".equals(scenes.packMode())
            || !releaseGate.approved(scenes.manifestSha256())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Recruitment is closed pending written ethics and data-protection approval");
        }
        if (!consent.informed() || !consent.adult() || !consent.voluntary() || !consent.researchMetrics()
            || !consent.deletionRights() || !consent.comprehensionPassed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Explicit consent is required");
        }
        String ip = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
        if (!limiter.allow("rate:enrollment:global", 500, Duration.ofMinutes(1))
            || !limiter.allow("rate:enrollment:network:" + crypto.keyedHandle(ip), 20, Duration.ofMinutes(15))) {
            throw new TooManyRequestsException(60);
        }
        UUID accountId = UUID.randomUUID();
        String handle = newHandle();
        while (accounts.findByHandle(handle).isPresent()) handle = newHandle();
        String csrf = token(24);
        String enrollmentId = token(32);
        var assigned = scenes.randomEnrollment();
        attempts.putEnrollment(enrollmentId, new AttemptStore.Enrollment(accountId, handle, csrf,
            assigned.stream().map(item -> item.scene().id()).toList(), java.time.OffsetDateTime.now(), null, 0));
        cookies.set(response, Cookies.ENROLLMENT, enrollmentId, properties.attemptTtlSeconds());
        return new StartResponse(csrf, handle, assigned.stream().map(item -> new SceneDto(
            item.scene().id(), item.scene().version(), item.scene().family(), item.scene().title(),
            item.scene().asset(), item.scene().thumbnail(), item.scene().license(),
            item.scene().eligibleCells().stream().sorted().toList())).toList());
    }

    @PostMapping("/confirmation")
    public ConfirmationResponse confirm(@Valid @RequestBody ConfirmationRequest request,
                                        @RequestHeader("X-CSRF-Token") String csrf,
                                        HttpServletRequest servletRequest) {
        String enrollmentId = Cookies.read(servletRequest.getCookies(), Cookies.ENROLLMENT);
        AttemptStore.Enrollment enrollment = enrollmentId == null ? null : attempts.getEnrollment(enrollmentId);
        if (enrollment == null || !constantEquals(enrollment.csrf(), csrf) || enrollment.matchingConfirmations() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enrollment confirmation unavailable");
        }
        byte[] canonical = canonical(request.stages(), enrollment);
        String tag = crypto.enrollmentConfirmationTag(canonical, enrollment.accountId());
        String expected = enrollment.confirmationTag();
        if (expected != null && !constantEquals(expected, tag)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The complete chain did not match");
        }
        int confirmations = expected == null ? 0 : 1;
        attempts.putEnrollment(enrollmentId, new AttemptStore.Enrollment(enrollment.accountId(), enrollment.handle(),
            enrollment.csrf(), enrollment.sceneIds(), enrollment.consentedAt(), tag, confirmations));
        return new ConfirmationResponse(confirmations, 2);
    }

    @PostMapping("/complete")
    @ResponseStatus(HttpStatus.CREATED)
    public CompleteResponse complete(@Valid @RequestBody CompleteRequest request,
                                     @RequestHeader("X-CSRF-Token") String csrf,
                                     HttpServletRequest servletRequest, HttpServletResponse response) {
        String enrollmentId = Cookies.read(servletRequest.getCookies(), Cookies.ENROLLMENT);
        AttemptStore.Enrollment enrollment = enrollmentId == null ? null : attempts.consumeEnrollment(enrollmentId);
        cookies.clear(response, Cookies.ENROLLMENT);
        if (enrollment == null || !constantEquals(enrollment.csrf(), csrf)
            || enrollment.confirmationTag() == null || enrollment.matchingConfirmations() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enrollment expired");
        }
        String normalizedPassword = request.password();
        if (BLOCKED.contains(normalizedPassword.strip().toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a less predictable study password");
        }
        var stages = stages(request.stages(), enrollment);
        byte[] canonical = Protocol.encode(stages);
        if (!constantEquals(enrollment.confirmationTag(),
                crypto.enrollmentConfirmationTag(canonical, enrollment.accountId()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The complete chain did not match");
        }
        byte[] salt = crypto.randomBytes(16);
        byte[] verifier = crypto.verifier(canonical, salt, enrollment.accountId());
        byte[] encrypted = crypto.encrypt(canonical, enrollment.accountId());
        byte[] passwordSalt = crypto.randomBytes(16);
        byte[] passwordVerifier = crypto.verifier(normalizedPassword.getBytes(StandardCharsets.UTF_8), passwordSalt, enrollment.accountId());
        accounts.create(enrollment.accountId(), enrollment.handle(), enrollment.consentedAt(), salt, verifier, encrypted,
            passwordSalt, passwordVerifier, enrollment.sceneIds(),
            enrollment.sceneIds().stream().map(id -> scenes.get(id).version()).toList());
        try {
            hotspots.increment(stages.stream().map(stage -> new HotspotAggregateRepository.Hotspot(
                stage.sceneId(), scenes.get(stage.sceneId()).version(), stage.cellId(), stage.actionId())).toList());
        } catch (RuntimeException ignored) {
            // Aggregate collection must never invalidate a successfully created credential.
        }
        try { studyEvents.record(enrollment.accountId(), "enrollment", "success", request.totalMs(),
            request.stageMs() == null ? List.of() : request.stageMs(), 0); }
        catch (RuntimeException ignored) { /* Metrics must not affect enrollment. */ }
        cookies.set(response, Cookies.SESSION, sessions.create(enrollment.accountId()), properties.sessionTtlSeconds());
        return new CompleteResponse(enrollment.handle());
    }

    private String newHandle() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        var out = new StringBuilder("SC-");
        for (int i = 0; i < 8; i++) {
            if (i == 4) out.append('-');
            out.append(alphabet.charAt(crypto.random().nextInt(alphabet.length())));
        }
        return out.toString();
    }

    private byte[] canonical(List<StageInput> input, AttemptStore.Enrollment enrollment) {
        return Protocol.encode(stages(input, enrollment));
    }

    private List<Protocol.Stage> stages(List<StageInput> input, AttemptStore.Enrollment enrollment) {
        if (input == null || input.size() != Protocol.STAGES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid enrollment");
        }
        var stages = new ArrayList<Protocol.Stage>();
        for (int i = 0; i < Protocol.STAGES; i++) {
            StageInput stage = input.get(i);
            if (stage.sceneId() != enrollment.sceneIds().get(i)
                || !scenes.get(stage.sceneId()).eligibleCells().contains(stage.cellId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid enrollment");
            }
            stages.add(new Protocol.Stage(stage.sceneId(), stage.cellId(), stage.actionId()));
        }
        return List.copyOf(stages);
    }

    private boolean constantEquals(String left, String right) {
        return left != null && right != null && java.security.MessageDigest.isEqual(
            left.getBytes(StandardCharsets.US_ASCII), right.getBytes(StandardCharsets.US_ASCII));
    }

    private String token(int bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(crypto.randomBytes(bytes));
    }
}
