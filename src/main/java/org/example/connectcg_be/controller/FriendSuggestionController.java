package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.connectcg_be.dto.FriendSuggestionDTO;
import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.FriendSuggestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/friends/suggestions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FriendSuggestionController {

    private final FriendSuggestionService friendSuggestionService;

    /**
     * API Lấy danh sách gợi ý kết bạn
     * GET /api/v1/friends/suggestions?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSuggestions(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        // Limit max size to 50
        if (size > 50) {
            size = 50;
        }
        
        Integer userId = currentUser.getId();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<FriendSuggestionDTO> suggestions = friendSuggestionService.getSuggestions(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("content", suggestions.getContent());
        data.put("page", suggestions.getNumber());
        data.put("size", suggestions.getSize());
        data.put("total_elements", suggestions.getTotalElements());
        data.put("total_pages", suggestions.getTotalPages());
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * API Ẩn/Xóa một gợi ý (Dismiss)
     * DELETE /api/v1/friends/suggestions/{userId}
     */
    @DeleteMapping("/{dismissedUserId}")
    public ResponseEntity<Map<String, String>> dismissSuggestion(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Integer dismissedUserId) {
        
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        Integer userId = currentUser.getId();
        friendSuggestionService.dismissSuggestion(userId, dismissedUserId);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Suggestion dismissed successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * API Làm mới gợi ý (Refresh) - Chỉ dành cho Admin hoặc System
     * POST /api/v1/friends/suggestions/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshSuggestions(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        Integer userId = currentUser.getId();
        friendSuggestionService.calculateSuggestions(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Suggestions refreshed for user");
        
        return ResponseEntity.ok(response);
    }
}
