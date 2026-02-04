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
            // Prompt design for strict SAFE/TOXIC moderation
            String prompt = "Phân tích nội dung sau xem có chứa từ ngữ thô tục, độc hại, xúc phạm, đả kích hay không. "
                    +
                    "Chỉ trả về JSON object duy nhất với định dạng: " +
                    "{\"label\": \"SAFE\" hoặc \"TOXIC\", \"reason\": \"giải thích ngắn gọn bằng tiếng Việt\"}. " +
                    "Nội dung cần phân tích: " + content;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Escape json content properly
            String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", " ");
            String requestStr = String.format(
                    "{\"contents\": [{\"parts\":[{\"text\": \"%s\"}]}]}", escapedPrompt);

            HttpEntity<String> entity = new HttpEntity<>(requestStr, headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl + "?key=" + apiKey, HttpMethod.POST, entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String aiResponse = response.getBody();

                // Extract JSON part using Jackson (assuming ObjectMapper is available or use
                // strict string parsing if simpler for now,
                // but user requested reliable parsing. Let's use simple string extraction
                // focused on the JSON block first to avoid dependency issues if Jackson isn't
                // configured,
                // actually the plan said use ObjectMapper. Let's add it.)

                // Extracting the JSON text from Gemini response structure: candidates ->
                // content -> parts -> text
                // Since adding Jackson dependency/bean might be complex in this snippet, I'll
                // use a robust string extraction specific for the EXPECTED JSON output.
                // NOTE: Detailed parsing loop below.

                // Locate the JSON block in the AI's textual response
                int jsonStart = aiResponse.indexOf("{", aiResponse.indexOf("\"text\":")); // Find start of our JSON
                                                                                          // inside the Gemini response
                                                                                          // text
                if (jsonStart == -1)
                    jsonStart = aiResponse.indexOf("{"); // Fallback

                if (jsonStart != -1) {
                    // Cleaner parsing approach:
                    // 1. Get the raw text returned by Gemini (which contains our JSON)
                    // ... parsing Gemini API response structure is nested.
                    // For safety and existing pattern compatibility, I will stick to finding the
                    // inner JSON.

                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(aiResponse);
                    String innerText = rootNode.path("candidates").get(0).path("content").path("parts").get(0)
                            .path("text").asText();

                    // Now parse our expected JSON
                    // Clean up markdown code blocks if any ```json ... ```
                    innerText = innerText.replaceAll("```json", "").replaceAll("```", "").trim();

                    com.fasterxml.jackson.databind.JsonNode resultNode = mapper.readTree(innerText);
                    String label = resultNode.path("label").asText().toUpperCase();
                    String reason = resultNode.path("reason").asText();

                    double score;
                    if ("SAFE".equals(label)) {
                        score = 0.1; // Safe -> Approved
                    } else {
                        score = 0.9; // TOXIC or unknown -> Pending
                        label = "TOXIC"; // Enforce TOXIC label for anything not SAFE
                    }

                    return new AiModerationResult(score, label, reason);
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini API call failed: " + e.getMessage());
        }

        // FAIL-SAFE: Any error -> PENDING (TOXIC)
        return new AiModerationResult(0.9, "AI_ERROR", "Lỗi kiểm duyệt AI - Cần duyệt thủ công");
    }
}
