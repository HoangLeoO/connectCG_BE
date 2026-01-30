package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendSuggestionDTO {
    private Integer userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    
    // Match info
    private BigDecimal totalScore;
    private String description; // "3 bạn chung, Cùng sống tại Hà Nội"
    
    // Profile preview
    private Integer age;
    private String occupation;
    private String city;
    
    // Metadata
    private Instant createdAt;
}
