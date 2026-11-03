package in.chaukas.web;

import in.chaukas.service.GeminiAnalyzer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final GeminiAnalyzer gemini;

    public HealthController(GeminiAnalyzer gemini) {
        this.gemini = gemini;
    }

    @GetMapping
    Map<String, Object> health() {
        return Map.of("status", "ok", "ai", gemini.available() ? "gemini" : "rules-only");
    }
}
