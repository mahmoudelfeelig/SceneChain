package de.scenechain.auth;

import de.scenechain.config.SceneChainProperties;
import de.scenechain.crypto.CredentialCrypto;
import de.scenechain.protocol.MarkerOverlay;
import de.scenechain.protocol.Protocol;
import de.scenechain.scene.SceneService;
import de.scenechain.study.StudyEventRepository;
import de.scenechain.study.StudySessionRepository;
import de.scenechain.user.AccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api")
public class AuthController {
    public record AttemptRequest(@NotBlank @Pattern(regexp = "SC-[A-Z2-9]{4}-[A-Z2-9]{4}") String handle,
                                 @NotBlank @Pattern(regexp = "direct|shielded") String mode) {}
    public record SceneDto(int id, int version, String family, String title, String asset, String thumbnail,
                           String license, List<Integer> overlay) {}
    public record AttemptResponse(String csrfToken, String mode, List<SceneDto> scenes) {}
    public record PasswordAttemptResponse(String csrfToken) {}
    public record PasswordAttemptRequest(@NotBlank @Pattern(regexp = "SC-[A-Z2-9]{4}-[A-Z2-9]{4}") String handle) {}
    public record StageResponse(@jakarta.validation.constraints.Min(1) int sceneId,
                                @jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(383) Integer cellId,
                                @jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(3) int actionId,
                                @jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(7) Integer markerId) {}
    public record CompleteRequest(@Size(min = 5, max = 5) List<@Valid StageResponse> stages,
                                  @jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(3600000) int totalMs,
                                  @Size(max = 5) List<@jakarta.validation.constraints.Min(0) Integer> stageMs) {}
    public record PasswordRequest(@NotBlank @Size(max = 128) String password) {}
    public record MeResponse(String handle, boolean studyRequired) {}
    public record Result(boolean authenticated) {}

    private final AccountRepository accounts;
    private final SceneService scenes;
    private final CredentialCrypto crypto;
    private final AttemptStore attempts;
    private final RateLimiter limiter;
    private final SessionStore sessions;
    private final Cookies cookies;
    private final StudyEventRepository studyEvents;
    private final SceneChainProperties properties;
    private final StudySessionRepository study;
    private final boolean recruitmentEnabled;
    private final byte[] dummySalt;
    private final byte[] dummyVerifier;
    private final UUID dummyId = new UUID(0, 0);

    public AuthController(AccountRepository accounts, SceneService scenes, CredentialCrypto crypto,
                          AttemptStore attempts, RateLimiter limiter, SessionStore sessions,
                          Cookies cookies, SceneChainProperties properties, StudyEventRepository studyEvents,
                          StudySessionRepository study,
                          @Value("${scenechain.recruitment-enabled:false}") boolean recruitmentEnabled) {
        this.accounts = accounts;
        this.scenes = scenes;
        this.crypto = crypto;
        this.attempts = attempts;
        this.limiter = limiter;
        this.sessions = sessions;
        this.cookies = cookies;
        this.studyEvents = studyEvents;
        this.properties = properties;
        this.study = study;
        this.recruitmentEnabled = recruitmentEnabled;
        this.dummySalt = crypto.randomBytes(16);
        this.dummyVerifier = crypto.verifier("scenechain-dummy".getBytes(StandardCharsets.UTF_8), dummySalt, dummyId);
    }

    @PostMapping("/auth/attempts")
    public AttemptResponse attempt(@Valid @RequestBody AttemptRequest request,
                                   HttpServletRequest servletRequest, HttpServletResponse response) {
        String handle = request.handle().toUpperCase(Locale.ROOT);
        String keyed = crypto.keyedHandle(handle);
        String ip = servletRequest.getRemoteAddr() == null ? "unknown" : servletRequest.getRemoteAddr();
        if (!limiter.allow("rate:attempt:global", 2000, Duration.ofMinutes(1))
            || !limiter.allow("rate:attempt:account:" + keyed, 20, Duration.ofMinutes(15))
            || !limiter.allow("rate:attempt:network:" + crypto.keyedHandle(ip), 100, Duration.ofMinutes(15))) {
            throw new TooManyRequestsException(60);
        }
        var account = accounts.findByHandle(handle).filter(AccountRepository.Account::enabled);
        if (recruitmentEnabled && account.isPresent() && !assigned(account.get().id(), request.mode())) {
            throw generic(HttpStatus.UNAUTHORIZED);
        }
        List<SceneService.Scene> cueScenes = account.isPresent()
            ? accounts.scenes(account.get().id()).stream().map(scenes::get).toList()
            : scenes.synthetic(handle);
        List<SceneService.Scene> publicPool = scenes.shuffledPublicPool();
        List<List<Integer>> overlays = new ArrayList<>();
        for (int i = 0; i < publicPool.size(); i++) {
            overlays.add("shielded".equals(request.mode()) ? MarkerOverlay.generate(crypto.random()) : List.of());
        }
        String attemptId = token(32);
        String csrf = token(24);
        attempts.putAttempt(attemptId, new AttemptStore.Attempt(keyed, account.map(AccountRepository.Account::id).orElse(null),
            account.isPresent(), request.mode(), csrf, cueScenes.stream().map(SceneService.Scene::id).toList(),
            publicPool.stream().map(SceneService.Scene::id).toList(), overlays, System.currentTimeMillis()));
        cookies.set(response, Cookies.ATTEMPT, attemptId, properties.attemptTtlSeconds());
        return new AttemptResponse(csrf, request.mode(), java.util.stream.IntStream.range(0, publicPool.size())
            .mapToObj(i -> new SceneDto(publicPool.get(i).id(), publicPool.get(i).version(), publicPool.get(i).family(),
                publicPool.get(i).title(), publicPool.get(i).asset(), publicPool.get(i).thumbnail(),
                publicPool.get(i).license(), overlays.get(i))).toList());
    }

    @PostMapping("/auth/attempts/complete")
    public Result complete(@Valid @RequestBody CompleteRequest request,
                           @RequestHeader("X-CSRF-Token") String csrf,
                           HttpServletRequest servletRequest, HttpServletResponse response) {
        String attemptId = Cookies.read(servletRequest.getCookies(), Cookies.ATTEMPT);
        AttemptStore.Attempt attempt = attemptId == null ? null : attempts.consumeAttempt(attemptId);
        cookies.clear(response, Cookies.ATTEMPT);
        if (attempt == null || !constantEquals(attempt.csrf(), csrf) || request.stages() == null
            || request.stages().size() != Protocol.STAGES) throw generic(HttpStatus.UNAUTHORIZED);
        if (!limiter.allow("rate:complete:global", 2000, Duration.ofMinutes(1))
            || !limiter.allow("rate:complete:account:" + attempt.keyedHandle(), 10, Duration.ofMinutes(15))) {
            throw new TooManyRequestsException(60);
        }
        int totalMs = elapsed(attempt);
        int recordedTotalMs = Math.min(totalMs, 180_000);
        List<Integer> stageMs = validatedStageTimes(request.stageMs(), recordedTotalMs);
        boolean timely = totalMs < 180_000;
        boolean valid = (attempt.known() ? verifyKnown(attempt, request.stages()) : verifyDummy(request.stages())) && timely;
        if (attempt.known()) {
            if (recruitmentEnabled) {
                try { study.recordTrial(attempt.accountId(), attempt.mode(), valid, recordedTotalMs, stageMs); }
                catch (RuntimeException error) { throw generic(HttpStatus.CONFLICT); }
            } else {
                try { studyEvents.record(attempt.accountId(), attempt.mode(), valid ? "success" : "failure",
                    recordedTotalMs, stageMs, 0); }
                catch (RuntimeException ignored) { /* Metrics must not affect authentication. */ }
            }
        }
        if (!valid) throw generic(HttpStatus.UNAUTHORIZED);
        String session = sessions.create(attempt.accountId());
        cookies.set(response, Cookies.SESSION, session, properties.sessionTtlSeconds());
        return new Result(true);
    }

    @PostMapping("/auth/password/attempts")
    public PasswordAttemptResponse passwordAttempt(@Valid @RequestBody PasswordAttemptRequest request,
                                                   HttpServletRequest servletRequest, HttpServletResponse response) {
        String handle = request.handle().toUpperCase(Locale.ROOT);
        String keyed = crypto.keyedHandle(handle);
        String ip = servletRequest.getRemoteAddr() == null ? "unknown" : servletRequest.getRemoteAddr();
        if (!limiter.allow("rate:password:global", 2000, Duration.ofMinutes(1))
            || !limiter.allow("rate:password:network:" + crypto.keyedHandle(ip), 100, Duration.ofMinutes(15))
            || !limiter.allow("rate:password:account:" + keyed, 10, Duration.ofMinutes(15))) {
            throw new TooManyRequestsException(60);
        }
        var account = accounts.findByHandle(handle).filter(AccountRepository.Account::enabled);
        if (recruitmentEnabled && account.isPresent() && !assigned(account.get().id(), "password")) {
            throw generic(HttpStatus.UNAUTHORIZED);
        }
        String csrf = token(24);
        String attemptId = token(32);
        attempts.putAttempt(attemptId, new AttemptStore.Attempt(keyed,
            account.map(AccountRepository.Account::id).orElse(null), account.isPresent(), "password", csrf,
            List.of(), List.of(), List.of(), System.currentTimeMillis()));
        cookies.set(response, Cookies.ATTEMPT, attemptId, properties.attemptTtlSeconds());
        return new PasswordAttemptResponse(csrf);
    }

    @PostMapping("/auth/password")
    public Result password(@Valid @RequestBody PasswordRequest request,
                           @RequestHeader("X-CSRF-Token") String csrf,
                           HttpServletRequest servletRequest, HttpServletResponse response) {
        String attemptId = Cookies.read(servletRequest.getCookies(), Cookies.ATTEMPT);
        AttemptStore.Attempt attempt = attemptId == null ? null : attempts.consumeAttempt(attemptId);
        cookies.clear(response, Cookies.ATTEMPT);
        if (attempt == null || !"password".equals(attempt.mode()) || !constantEquals(attempt.csrf(), csrf)) {
            throw generic(HttpStatus.UNAUTHORIZED);
        }
        int totalMs = elapsed(attempt);
        boolean valid;
        UUID id;
        if (attempt.known()) {
            id = attempt.accountId();
            var credential = accounts.password(id).orElseThrow(() -> generic(HttpStatus.UNAUTHORIZED));
            valid = crypto.verify(request.password().getBytes(StandardCharsets.UTF_8), credential.salt(), id, credential.verifier());
        } else {
            id = dummyId;
            valid = crypto.verify(request.password().getBytes(StandardCharsets.UTF_8), dummySalt, dummyId, dummyVerifier) && false;
        }
        valid &= totalMs < 180_000;
        if (attempt.known()) {
            if (recruitmentEnabled) {
                try { study.recordTrial(id, "password", valid, Math.min(totalMs, 180_000), List.of()); }
                catch (RuntimeException error) { throw generic(HttpStatus.CONFLICT); }
            } else {
                try { studyEvents.record(id, "password", valid ? "success" : "failure", Math.min(totalMs, 180_000), List.of(), 0); }
                catch (RuntimeException ignored) { /* Research collection cannot change authentication. */ }
            }
        }
        if (!valid) throw generic(HttpStatus.UNAUTHORIZED);
        cookies.set(response, Cookies.SESSION, sessions.create(id), properties.sessionTtlSeconds());
        return new Result(true);
    }

    @GetMapping("/me")
    public MeResponse me(HttpServletRequest request) {
        UUID id = sessions.get(Cookies.read(request.getCookies(), Cookies.SESSION));
        if (id == null) throw generic(HttpStatus.UNAUTHORIZED);
        return accounts.findById(id).filter(AccountRepository.Account::enabled)
            .map(account -> new MeResponse(account.handle(), recruitmentEnabled))
            .orElseThrow(() -> generic(HttpStatus.UNAUTHORIZED));
    }

    @DeleteMapping("/session")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = Cookies.read(request.getCookies(), Cookies.SESSION);
        sessions.delete(token);
        cookies.clear(response, Cookies.SESSION);
    }

    private boolean verifyKnown(AttemptStore.Attempt attempt, List<StageResponse> input) {
        var credential = accounts.credential(attempt.accountId()).orElse(null);
        if (credential == null) return false;
        try {
            if ("direct".equals(attempt.mode())) {
                var stages = new ArrayList<Protocol.Stage>();
                for (int i = 0; i < Protocol.STAGES; i++) {
                    StageResponse stage = input.get(i);
                    if (stage.cellId() == null) return false;
                    stages.add(new Protocol.Stage(stage.sceneId(), stage.cellId(), stage.actionId()));
                }
                return crypto.verify(Protocol.encode(stages), credential.salt(), attempt.accountId(), credential.verifier());
            }
            byte[] canonical = crypto.decrypt(credential.encryptedMetadata(), attempt.accountId());
            boolean valid = crypto.verify(canonical, credential.salt(), attempt.accountId(), credential.verifier());
            List<Protocol.Stage> enrolled = Protocol.decode(canonical);
            int mismatches = 0;
            for (int i = 0; i < Protocol.STAGES; i++) {
                StageResponse response = input.get(i);
                Protocol.Stage expected = enrolled.get(i);
                int poolIndex = attempt.publicSceneIds().indexOf(response.sceneId());
                if (poolIndex < 0) return false;
                int marker = MarkerOverlay.expected(attempt.overlays().get(poolIndex), expected.cellId());
                mismatches |= response.sceneId() ^ expected.sceneId();
                mismatches |= response.actionId() ^ expected.actionId();
                mismatches |= (response.markerId() == null ? -1 : response.markerId()) ^ marker;
            }
            return valid && mismatches == 0;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean verifyDummy(List<StageResponse> stages) {
        byte[] candidate = new byte[39];
        for (int i = 0; i < Math.min(stages.size(), 5); i++) candidate[i] = (byte) stages.get(i).actionId();
        crypto.verify(candidate, dummySalt, dummyId, dummyVerifier);
        return false;
    }

    private boolean constantEquals(String a, String b) {
        if (a == null || b == null) return false;
        return java.security.MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private String token(int bytes) { return Base64.getUrlEncoder().withoutPadding().encodeToString(crypto.randomBytes(bytes)); }
    private int elapsed(AttemptStore.Attempt attempt) {
        long value = Math.max(0, System.currentTimeMillis() - attempt.startedAtMillis());
        return (int) Math.min(Integer.MAX_VALUE, value);
    }
    private List<Integer> validatedStageTimes(List<Integer> values, int totalMs) {
        if (values == null || values.isEmpty()) return List.of();
        long sum = values.stream().mapToLong(Integer::longValue).sum();
        if (values.size() != Protocol.STAGES || sum > totalMs) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stage timing");
        }
        return List.copyOf(values);
    }
    private boolean assigned(UUID accountId, String condition) {
        if (study == null) return false;
        var state = study.find(accountId);
        return state != null && condition.equals(state.condition())
            && ("practice".equals(state.phase()) || "measured".equals(state.phase())
                || ("retention".equals(state.phase()) && state.retentionReady()));
    }
    private ResponseStatusException generic(HttpStatus status) { return new ResponseStatusException(status, "Authentication failed"); }
}
