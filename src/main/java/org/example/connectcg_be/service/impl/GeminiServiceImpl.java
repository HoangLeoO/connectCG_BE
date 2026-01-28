package org.example.connectcg_be.service.impl;

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
    public String checkPostContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "SAFE";
        }

        try {
            String url = apiUrl + "?key=" + apiKey;

            // Prompt design for moderation
            String prompt = "Xác định xem nội dung bài đăng sau đây có chứa từ ngữ thô tục, xúc phạm, thù ghét hoặc không phù hợp hay không. "
                    +
                    "Nếu có vi phạm, hãy trả về 'TOXIC'. Nếu an toàn, hãy trả về 'SAFE'. " +
                    "Chỉ trả về đúng một từ duy nhất ('TOXIC' hoặc 'SAFE'). " +
                    "Nội dung: " + content;

            // Prepare request body for Gemini API
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contentMap = new HashMap<>();
            Map<String, String> partMap = new HashMap<>();
            partMap.put("text", prompt);
            contentMap.put("parts", Collections.singletonList(partMap));
            requestBody.put("contents", Collections.singletonList(contentMap));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate
                    .exchange(url, HttpMethod.POST, entity,
                            (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> outputContent = (Map<String, Object>) candidate.get("content");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) outputContent.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map<String, Object> part = parts.get(0);
                        String text = (String) part.get("text");
                        if (text != null) {
                            String result = text.trim().toUpperCase();
                            return result.contains("TOXIC") ? "TOXIC" : "SAFE";
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini API call failed: " + e.getMessage());
        }

        return "SAFE"; // Fallback to SAFE
    }
}
