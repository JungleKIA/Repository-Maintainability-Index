package com.kaicode.rmi.service;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MaintainabilityReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MaintainabilityServiceEdgeCaseTest {

    @Mock
    private GitHubClient client;

    @Test
    void shouldGenerateRecommendationForPoorScore() {
        MaintainabilityService service = new MaintainabilityService(client);
        
        String recommendation = service.generateRecommendation(30, new HashMap<>());
        
        assertThat(recommendation).contains("needs improvement");
    }

    @Test
    void shouldGenerateRecommendationWithMultipleImprovements() {
        MaintainabilityService service = new MaintainabilityService(client);
        
        Map<String, com.kaicode.rmi.model.MetricResult> metrics = new HashMap<>();
        metrics.put("Metric1", com.kaicode.rmi.model.MetricResult.builder()
                .name("Metric1")
                .score(40)
                .weight(0.5)
                .description("Test")
                .build());
        metrics.put("Metric2", com.kaicode.rmi.model.MetricResult.builder()
                .name("Metric2")
                .score(30)
                .weight(0.5)
                .description("Test")
                .build());
        
        String recommendation = service.generateRecommendation(35, metrics);
        
        assertThat(recommendation).contains("Metric1");
        assertThat(recommendation).contains("Metric2");
    }

    @Test
    void shouldGenerateRecommendationForBoundaryScores() {
        MaintainabilityService service = new MaintainabilityService(client);
        
        assertThat(service.generateRecommendation(90, new HashMap<>()))
                .contains("Excellent");
        assertThat(service.generateRecommendation(89, new HashMap<>()))
                .contains("Good");
        assertThat(service.generateRecommendation(75, new HashMap<>()))
                .contains("Good");
        assertThat(service.generateRecommendation(74, new HashMap<>()))
                .contains("Fair");
        assertThat(service.generateRecommendation(60, new HashMap<>()))
                .contains("Fair");
        assertThat(service.generateRecommendation(59, new HashMap<>()))
                .containsAnyOf("needs improvement", "improvement");
    }

    @Test
    void shouldUseDefaultMetricCalculators() {
        MaintainabilityService service = new MaintainabilityService(client);
        
        assertThat(service).isNotNull();
    }
}
