package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.AiModerationResult;
import org.example.connectcg_be.service.AiModerationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenRouterServiceImpl implements AiModerationService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model}")
    private String model;

    @Value("${openrouter.site.url}")
    private String siteUrl;

    @Value("${openrouter.site.name}")
    private String siteName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AiModerationResult checkPostContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new AiModerationResult(0.0, "SAFE", "No content to check");
        }

        try {
            // Prompt design for strict SAFE/TOXIC moderation
            String prompt = "Phân tích nội dung sau xem có chứa từ ngữ thô tục, độc hại, xúc phạm, đả kích hay không. "
                    + "Chỉ trả về JSON object duy nhất với định dạng: "
                    + "{\"label\": \"SAFE\" hoặc \"TOXIC\", \"reason\": \"giải thích ngắn gọn bằng tiếng Việt\"}. "
                    + "Nội dung cần phân tích: " + content;

            // Build OpenRouter API request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", siteUrl);
            headers.set("X-Title", siteName);

            // OpenRouter uses OpenAI-compatible Chat Completions format
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);

            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String aiResponse = response.getBody();

                // Parse OpenRouter response (OpenAI-compatible format)
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(aiResponse);

                // Extract content from: choices[0].message.content
                String responseContent = rootNode.path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();

                // Clean up markdown code blocks if any ```json ... ```
                responseContent = responseContent.replaceAll("```json", "").replaceAll("```", "").trim();

                // Parse our expected JSON response
                com.fasterxml.jackson.databind.JsonNode resultNode = mapper.readTree(responseContent);
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
        } catch (Exception e) {
            System.err.println("OpenRouter API call failed: " + e.getMessage());
            e.printStackTrace();
        }

        // FAIL-SAFE: Any error -> PENDING (TOXIC)
        return new AiModerationResult(0.9, "AI_ERROR", "Lỗi kiểm duyệt AI - Cần duyệt thủ công");
    }
}
