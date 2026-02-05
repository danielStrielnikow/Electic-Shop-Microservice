package pl.electricshop.ai_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import pl.electricshop.ai_service.api.response.RecommendationResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendationService {

    private final ChatClient.Builder chatClientBuilder;

    public RecommendationResponse getRecommendations(String productName) {
        var chatClient = chatClientBuilder.build();

        // Prompt inżynieria: Prosimy o konkretny format (lista po przecinku)
        String promptText = """
                Jesteś ekspertem w sklepie z elektroniką.
                Klient ogląda produkt: {product}.
                
                Twoje zadanie:
                Zaproponuj 3 konkretne akcesoria lub produkty komplementarne, które pasują do tego produktu.
                Wymień tylko nazwy produktów oddzielone przecinkami. Nie używaj numeracji.
                Przykład: Etui ochronne, Ładowarka sieciowa, Słuchawki bezprzewodowe
                
                Odpowiedź ma być krótka i zwięzła.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        String rawResponse = chatClient
                .prompt(promptTemplate.create(Map.of("product", productName))) // 1. Ładujemy prompt
                .call()       // 2. Wywołujemy (Call)
                .content();

        log.info("Surowa odpowiedź DeepSeek: {}", rawResponse);

        // DeepSeek R1 często zwraca sekcję <think>, musimy ją usunąć
        String cleanResponse = removeThinkTag(rawResponse).trim();

        // Parsowanie odpowiedzi do listy
        List<String> suggestions = Arrays.stream(cleanResponse.split(","))
                .map(String::trim)
                .toList();

        return new RecommendationResponse(productName, suggestions, "Wygenerowano przez DeepSeek Local");
    }

    // Metoda pomocnicza do usuwania procesu myślowego DeepSeek (<think>...</think>)
    private String removeThinkTag(String response) {
        if (response.contains("</think>")) {
            return response.substring(response.indexOf("</think>") + 8);
        }
        return response;
    }
}
