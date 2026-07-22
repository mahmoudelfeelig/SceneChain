package de.scenechain.study;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/admin/research")
public class ResearchOutcomeController {
    private static final String HANDLE = "SC-[A-Z2-9]{4}-[A-Z2-9]{4}";
    public record ObserverRequest(@NotBlank @Pattern(regexp=HANDLE) String handle,
                                  @NotBlank @Pattern(regexp="direct|shielded") String condition,
                                  boolean completeChainSuccess, @Min(1) @Max(1) int observationCount,
                                  @Min(1) @Max(1) int attemptCount, boolean recordingUsed) {}
    public record LockoutRequest(@NotBlank @Pattern(regexp=HANDLE) String handle,
                                 @Min(1) @Max(20) int attemptsUntilThrottle,
                                 @Min(1) @Max(3600) int retryAfterSeconds,
                                 boolean waitCommunicated, boolean disposableAccount) {}
    public record ReportRequest(@NotBlank @Pattern(regexp=HANDLE) String handle,
                                @NotBlank @Pattern(regexp="none|visual|motor|cognitive|multiple|other") String accessibilityCode,
                                boolean recoveryUsed) {}

    private final ResearchOutcomeRepository outcomes;
    private final ResearchAdminAccess admin;

    public ResearchOutcomeController(ResearchOutcomeRepository outcomes, ResearchAdminAccess admin) {
        this.outcomes = outcomes; this.admin = admin;
    }

    @PostMapping("/observer")
    public void observer(@RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorization,
                         @Valid @RequestBody ObserverRequest body, HttpServletRequest request) {
        String actor = admin.authorize(authorization, request, "observer_outcome");
        if (body.recordingUsed() || body.observationCount() != 1 || body.attemptCount() != 1
            || !outcomes.observer(body.handle(), body.condition(), body.completeChainSuccess())) {
            admin.denied(actor, "observer_outcome"); throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        admin.success(actor, "observer_outcome");
    }

    @PostMapping("/lockout")
    public void lockout(@RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorization,
                        @Valid @RequestBody LockoutRequest body, HttpServletRequest request) {
        String actor = admin.authorize(authorization, request, "lockout_outcome");
        if (!body.disposableAccount() || !outcomes.lockout(body.handle(), body.attemptsUntilThrottle(),
                body.retryAfterSeconds(), body.waitCommunicated())) {
            admin.denied(actor, "lockout_outcome"); throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        admin.success(actor, "lockout_outcome");
    }

    @PostMapping("/participant-report")
    public void report(@RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorization,
                       @Valid @RequestBody ReportRequest body, HttpServletRequest request) {
        String actor = admin.authorize(authorization, request, "participant_report");
        if (!outcomes.report(body.handle(), body.accessibilityCode(), body.recoveryUsed())) {
            admin.denied(actor, "participant_report"); throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        admin.success(actor, "participant_report");
    }

    @GetMapping(value="/outcomes.csv", produces="text/csv")
    public ResponseEntity<StreamingResponseBody> export(
            @RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorization,
            @RequestParam(required=false) OffsetDateTime from, @RequestParam(required=false) OffsetDateTime to,
            @RequestParam(defaultValue="10000") int limit, HttpServletRequest request) {
        String actor = admin.authorize(authorization, request, "outcome_export");
        if (from == null || to == null || limit < 1 || limit > 10_000 || !from.isBefore(to)
            || from.isBefore(OffsetDateTime.now().minusYears(3))) {
            admin.denied(actor, "outcome_export"); throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        var rows = outcomes.export(from, to, limit);
        admin.success(actor, "outcome_export");
        StreamingResponseBody stream = output -> {
            var writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
            writer.write("outcome_id,subject_pseudonym,outcome_type,condition,primary_value,secondary_value,created_at\n");
            for (var row : rows) writer.write(row.id() + "," + admin.pseudonym(row.subjectId().toString()) + ","
                + row.type() + "," + value(row.condition()) + "," + row.primaryValue() + ","
                + row.secondaryValue() + "," + row.createdAt() + "\n");
            writer.flush();
        };
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=scenechain-secondary-outcomes.csv")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8)).body(stream);
    }

    private String value(String value) { return value == null ? "" : value; }
}
