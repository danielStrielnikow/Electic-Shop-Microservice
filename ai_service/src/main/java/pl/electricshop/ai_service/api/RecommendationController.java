package pl.electricshop.ai_service.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.electricshop.ai_service.api.response.RecommendationResponse;
import pl.electricshop.ai_service.service.AiRecommendationService;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final AiRecommendationService recommendationService;

    @GetMapping("/{productName}")
    public ResponseEntity<RecommendationResponse> getRecommendations(@PathVariable String productName) {
        RecommendationResponse response = recommendationService.getRecommendations(productName);
        return ResponseEntity.ok(response);
    }
}
