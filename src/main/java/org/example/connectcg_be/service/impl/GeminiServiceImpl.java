package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.AiModerationResult;
import org.example.connectcg_be.service.GeminiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiServiceImpl implements GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AiModerationResult checkPostContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new AiModerationResult(0.0, "SAFE", "No content to check");
        }

        try {
            String url = apiUrl + "?key=" + apiKey;

            // Prompt design for moderation
            String prompt = "Analyze the following content for toxicity and hate speech. " +
                    "Return ONLY a JSON object with this structure: " +
                    "{\"score\": double (0.0 to 1.0), \"label\": \"SAFE\"|\"SUSPICIOUS\"|\"TOXIC\", \"reason\": \"string description in Vietnamese\"}. "
                    +
                    "Content to analyze: " + content;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestStr = String.format(
                    "{\"contents\": [{\"parts\":[{\"text\": \"%s\"}]}]}",
                    prompt.replace("\"", "\\\"").replace("\n", " "));

            HttpEntity<String> entity = new HttpEntity<>(requestStr, headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl + "?key=" + apiKey, HttpMethod.POST, entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String aiResponse = response.getBody();

                // Extraction logic
                if (aiResponse.contains("{") && aiResponse.contains("}")) {
                    String jsonPart = aiResponse.substring(aiResponse.indexOf("{"), aiResponse.lastIndexOf("}") + 1);

                    // Simple extraction (mock for now, should use Jackson in real)
                    double score = 0.0;
                    if (jsonPart.contains("TOXIC"))
                        score = 0.9;
                    else if (jsonPart.contains("SUSPICIOUS"))
                        score = 0.5;

                    String label = jsonPart.contains("TOXIC") ? "TOXIC"
                            : (jsonPart.contains("SUSPICIOUS") ? "SUSPICIOUS" : "SAFE");
                    String reason = "Nội dung được phân tích bởi AI";

                    return new AiModerationResult(score, label, reason);
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini API call failed: " + e.getMessage());
        }

        return new AiModerationResult(0.0, "NOT_CHECKED", "API Error or No Result");
    }
}
