package de.scenechain.study;

import jakarta.servlet.http.HttpServletRequest;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/admin/research")
public class StudyExportController {
    private final StudyEventRepository events;
    private final ResearchAdminAccess admin;

    public StudyExportController(StudyEventRepository events, ResearchAdminAccess admin) {
        this.events = events; this.admin = admin;
    }

    @GetMapping(value = "/events.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> export(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(defaultValue = "10000") int limit, HttpServletRequest request) {
        String actor = admin.authorize(authorization, request, "research_export");
        if (from == null || to == null || limit < 1 || limit > 10_000 || !from.isBefore(to)
            || from.isBefore(OffsetDateTime.now().minusYears(3))) {
            admin.denied(actor, "research_export");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bounded export window");
        }
        var rows = events.exportRows(from, to, limit);
        admin.success(actor, "research_export");
        StreamingResponseBody stream = output -> {
            var writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
            writer.write("event_id,subject_pseudonym,condition,outcome,total_ms,stage_ms,retry_count,period,phase,trial_number,first_attempt,timed_out,viewport_class,input_method,browser_family,deviation_code,system_failure,created_at\n");
            for (var row : rows) {
                writer.write(row.id() + "," + admin.pseudonym(row.subjectId().toString()) + "," + row.condition() + ","
                    + row.outcome() + "," + row.totalMs() + ",\"" + java.util.Arrays.toString(row.stageMs())
                    + "\"," + row.retryCount() + "," + value(row.period()) + "," + value(row.phase()) + ","
                    + value(row.trialNumber()) + "," + value(row.firstAttempt()) + "," + row.timedOut() + ","
                    + value(row.viewportClass()) + "," + value(row.inputMethod()) + "," + value(row.browserFamily()) + ","
                    + value(row.deviationCode()) + "," + row.systemFailure() + "," + row.createdAt() + "\n");
            }
            writer.flush();
        };
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=scenechain-study-events.csv")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8)).body(stream);
    }

    private String value(Object value) { return value == null ? "" : value.toString(); }
}
