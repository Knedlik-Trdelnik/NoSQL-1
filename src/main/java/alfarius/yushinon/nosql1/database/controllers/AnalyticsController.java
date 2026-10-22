package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.database.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/popular-tags")
    public ResponseEntity<?> getPopularTags() {
        return ResponseEntity.ok(analyticsService.getMostFrequentTags());
    }

    @GetMapping("/explain")
    public ResponseEntity<String> getQueryExplain(
            @RequestParam String ruleId,
            @RequestParam @DateTimeFormat LocalDateTime date) {
        String explainResult = analyticsService.explainQuery(ruleId, date);
        return ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(explainResult);
    }
}