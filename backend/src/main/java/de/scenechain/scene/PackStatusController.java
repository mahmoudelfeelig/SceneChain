package de.scenechain.scene;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import de.scenechain.study.ReleaseGateRepository;

@RestController
@RequestMapping("/api/pack")
public class PackStatusController {
    private final SceneService scenes;
    private final boolean recruitmentEnabled;
    private final ReleaseGateRepository releaseGate;

    public PackStatusController(SceneService scenes,
                                @Value("${scenechain.recruitment-enabled:false}") boolean recruitmentEnabled,
                                ReleaseGateRepository releaseGate) {
        this.scenes = scenes;
        this.recruitmentEnabled = recruitmentEnabled;
        this.releaseGate = releaseGate;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        boolean ready = "formal".equals(scenes.packMode()) && recruitmentEnabled
            && releaseGate.approved(scenes.manifestSha256());
        return Map.of("mode", scenes.packMode(), "status", scenes.packStatus(), "sceneCount", scenes.all().size(),
            "manifestSha256", scenes.manifestSha256(), "recruitmentEnabled", ready);
    }
}
