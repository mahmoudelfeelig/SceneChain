package de.scenechain.study;

import de.scenechain.auth.Cookies;
import de.scenechain.auth.SessionStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/study")
public class StudyController {
    public record StartRequest(@Min(1024) @Max(10000) int viewportWidth,
                               @Min(600) @Max(10000) int viewportHeight,
                               @Pattern(regexp="mouse|trackpad|keyboard|touch|other") String inputMethod,
                               @Pattern(regexp="chromium|firefox|safari|other") String browserFamily) {}
    public record WorkloadRequest(@Min(0) @Max(20) int mental, @Min(0) @Max(20) int physical,
                                  @Min(0) @Max(20) int temporal, @Min(0) @Max(20) int performance,
                                  @Min(0) @Max(20) int effort, @Min(0) @Max(20) int frustration) {}
    public record StateResponse(String condition, String phase, int period, int trialNumber,
                                int practiceSuccesses, int retentionPeriod, boolean retentionReady, String retentionDueAt,
                                boolean complete) {}

    private final SessionStore sessions;
    private final StudySessionRepository study;

    public StudyController(SessionStore sessions, StudySessionRepository study) {
        this.sessions = sessions; this.study = study;
    }

    @PostMapping("/start")
    public StateResponse start(@Valid @RequestBody StartRequest request, HttpServletRequest servletRequest) {
        return response(study.start(account(servletRequest), request.viewportWidth(), request.viewportHeight(),
            request.inputMethod(), request.browserFamily()));
    }

    @GetMapping("/state")
    public StateResponse state(HttpServletRequest request) {
        var value = study.find(account(request));
        if (value == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Study session not started");
        return response(value);
    }

    @PostMapping("/workload")
    public StateResponse workload(@Valid @RequestBody WorkloadRequest request, HttpServletRequest servletRequest) {
        try {
            return response(study.workload(account(servletRequest), request.mental(), request.physical(),
                request.temporal(), request.performance(), request.effort(), request.frustration()));
        } catch (IllegalStateException error) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Study transition rejected");
        }
    }

    private UUID account(HttpServletRequest request) {
        UUID id = sessions.get(Cookies.read(request.getCookies(), Cookies.SESSION));
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return id;
    }

    private StateResponse response(StudySessionRepository.State state) {
        return new StateResponse(state.condition(), state.phase(), state.period(), state.trialNumber(),
            state.practiceSuccesses(), state.retentionPeriod(), state.retentionReady(),
            state.retentionDueAt() == null ? null : state.retentionDueAt().toString(), "complete".equals(state.phase()));
    }
}
