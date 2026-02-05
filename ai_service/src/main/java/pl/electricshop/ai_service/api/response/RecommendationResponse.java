package pl.electricshop.ai_service.api.response;

import java.util.List;

public record RecommendationResponse(
        String forProduct,
        List<String> suggestions,
        String aiReasoning
) {
}
