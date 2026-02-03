package org.example.connectcg_be.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.connectcg_be.dto.FriendSuggestionDTO;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.FriendSuggestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendSuggestionServiceImpl implements FriendSuggestionService {

    private final FriendSuggestionRepository friendSuggestionRepository;
    private final DismissedSuggestionRepository dismissedSuggestionRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public Page<FriendSuggestionDTO> getSuggestions(Integer userId, Pageable pageable) {
        log.info("Fetching suggestions for user: {}", userId);
        
        // 1. Kiểm tra xem đã có cache gợi ý hợp lệ chưa (còn hạn sử dụng)
        boolean hasValidCache = friendSuggestionRepository.existsByUserIdAndExpiresAtAfter(userId, Instant.now());
        
        // 2. Nếu chưa có hoặc cache hết hạn -> Tính toán lại gợi ý mới
        if (!hasValidCache) {
            log.info("No valid cache found, calculating new suggestions for user: {}", userId);
            calculateSuggestions(userId);
        }
        
        // 3. Lấy danh sách gợi ý từ Database
        Page<FriendSuggestion> suggestions = friendSuggestionRepository.findActiveByUserId(userId, Instant.now(), pageable);
        
        // 4. Chuyển đổi sang DTO để trả về Frontend
        List<FriendSuggestionDTO> dtos = new ArrayList<>();
        for (FriendSuggestion suggestion : suggestions.getContent()) {
            FriendSuggestionDTO dto = mapToDTO(suggestion);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        
        return new PageImpl<>(dtos, pageable, suggestions.getTotalElements());
    }

    @Override
    @Transactional
    public void calculateSuggestions(Integer userId) {
        log.info("Calculating suggestions for user: {}", userId);
        
        try {
            // 1. Xóa các gợi ý cũ của user này để tính lại từ đầu
            jdbcTemplate.update("DELETE FROM friend_suggestions WHERE user_id = ?", userId);
            

            // 2. Thuật toán tính điểm và tìm gợi ý
            // CTE (Common Table Expressions) giúp code SQL dễ đọc hơn
            String sql = """
                INSERT INTO friend_suggestions (user_id, suggested_user_id, score, reason, expires_at, created_at)
                WITH MutualFriends AS (
                    -- Chiến lược 1: Tìm qua Bạn chung (Mutual Friends)
                    -- A là bạn B, B là bạn C -> Gợi ý C cho A
                    SELECT 
                        f2.friend_id AS candidate_id, -- Người được gợi ý (C)
                        COUNT(f2.friend_id) * 10 AS score, -- Mỗi bạn chung = 10 điểm
                        CONCAT(COUNT(f2.friend_id), ' bạn chung') AS reason_detail
                    FROM friends f1
                    JOIN friends f2 ON f1.friend_id = f2.user_id
                    WHERE f1.user_id = ?
                      AND f2.friend_id != ?
                      AND f2.friend_id NOT IN (
                          SELECT friend_id FROM friends WHERE user_id = ?
                      )
                      AND f2.friend_id NOT IN (
                          SELECT sender_id FROM friend_requests 
                          WHERE receiver_id = ? AND status = 'PENDING'
                          UNION
                          SELECT receiver_id FROM friend_requests 
                          WHERE sender_id = ? AND status = 'PENDING'
                      )
                      AND f2.friend_id NOT IN (
                          SELECT dismissed_user_id FROM dismissed_suggestions 
                          WHERE user_id = ?
                      )
                      AND f2.friend_id NOT IN (
                          SELECT id FROM users WHERE is_deleted = TRUE OR is_locked = TRUE
                      )
                    GROUP BY f2.friend_id
                    HAVING COUNT(f2.friend_id) >= 1
                ),
                
                SameCity AS (
                    -- Chiến lược 2: Tìm người Cùng thành phố
                    SELECT 
                        u.id AS candidate_id,
                        5 AS score, -- Cùng thành phố = 5 điểm
                        CONCAT('Cùng sống tại ', up.city_name) AS reason_detail
                    FROM users u
                    JOIN user_profiles up ON u.id = up.user_id
                    JOIN user_profiles my_profile ON my_profile.user_id = ?
                    WHERE up.city_name = my_profile.city_name
                      AND up.city_name IS NOT NULL
                      AND up.city_name != ''
                      AND u.id != ?
                      AND u.is_deleted = FALSE
                      AND u.is_locked = FALSE
                      AND u.id NOT IN (
                          SELECT friend_id FROM friends WHERE user_id = ?
                      )
                      AND u.id NOT IN (
                          SELECT sender_id FROM friend_requests 
                          WHERE receiver_id = ? AND status = 'PENDING'
                          UNION
                          SELECT receiver_id FROM friend_requests 
                          WHERE sender_id = ? AND status = 'PENDING'
                      )
                      AND u.id NOT IN (
                          SELECT dismissed_user_id FROM dismissed_suggestions 
                          WHERE user_id = ?
                      )
                ),
                
                SameHobby AS (
                    SELECT
                        uh2.user_id AS candidate_id,
                        COUNT(uh2.hobby_id) * 7 AS score,
                        CONCAT(COUNT(uh2.hobby_id), ' sở thích chung') AS reason_detail
                    FROM user_hobbies uh1
                    JOIN user_hobbies uh2 ON uh1.hobby_id = uh2.hobby_id
                    JOIN users u ON u.id = uh2.user_id
                    WHERE uh1.user_id = ?
                      AND uh2.user_id != ?
                      AND u.is_deleted = FALSE
                      AND u.is_locked = FALSE
                      AND uh2.user_id NOT IN (
                          SELECT friend_id FROM friends WHERE user_id = ?
                      )
                      AND uh2.user_id NOT IN (
                          SELECT sender_id FROM friend_requests 
                          WHERE receiver_id = ? AND status = 'PENDING'
                          UNION
                          SELECT receiver_id FROM friend_requests 
                          WHERE sender_id = ? AND status = 'PENDING'
                      )
                      AND uh2.user_id NOT IN (
                          SELECT dismissed_user_id FROM dismissed_suggestions 
                          WHERE user_id = ?
                      )
                    GROUP BY uh2.user_id
                    HAVING COUNT(uh2.hobby_id) >= 1
                ),

                FinalCandidates AS (
                    -- Tổng hợp kết quả từ các chiến lược
                    SELECT 
                        candidate_id,
                        SUM(score) AS total_score,
                        GROUP_CONCAT(reason_detail ORDER BY score DESC SEPARATOR ', ') AS combined_reasons
                    FROM (
                        SELECT * FROM MutualFriends
                        UNION ALL
                        SELECT * FROM SameCity
                        UNION ALL
                        SELECT * FROM SameHobby
                    ) AS AllSources
                    GROUP BY candidate_id
                    HAVING total_score >= 5
                )
                SELECT 
                    ?, 
                    candidate_id, 
                    total_score, 
                    combined_reasons,
                    DATE_ADD(NOW(), INTERVAL 24 HOUR),
                    NOW()
                FROM FinalCandidates
                ORDER BY total_score DESC
                LIMIT 10
                """;
            
            // Execute với 19 parameters (userId được dùng nhiều lần)
            int rowsAffected = jdbcTemplate.update(sql, 
                userId, userId, userId,  // MutualFriends
                userId, userId, userId,  // MutualFriends filters
                userId, userId, userId,  // SameCity
                userId, userId, userId,  // SameCity filters
                userId, userId,          // SameHobby
                userId, userId, userId,  // SameHobby filters
                userId,                  // SameHobby filters
                userId                   // Final INSERT
            );
            
            log.info("Calculated {} suggestions for user: {}", rowsAffected, userId);
            
        } catch (Exception e) {
            log.error("Error calculating suggestions for user: {}", userId, e);
            throw new RuntimeException("Failed to calculate suggestions", e);
        }
    }

    @Override
    @Transactional
    public void dismissSuggestion(Integer userId, Integer dismissedUserId) {
        log.info("User {} dismissing suggestion: {}", userId, dismissedUserId);
        
        // 1. Xóa khỏi bảng gợi ý hiện tại
        friendSuggestionRepository.deleteByUserIdAndSuggestedUserId(userId, dismissedUserId);
        
        // 2. Thêm vào bảng "Đã bỏ qua" để không bao giờ gợi ý lại nữa
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User dismissedUser = userRepository.findById(dismissedUserId)
                .orElseThrow(() -> new RuntimeException("Dismissed user not found"));
        
        DismissedSuggestion dismissed = new DismissedSuggestion();
        DismissedSuggestionId id = new DismissedSuggestionId();
        id.setUserId(userId);
        id.setDismissedUserId(dismissedUserId);
        dismissed.setId(id);
        dismissed.setUser(user);
        dismissed.setDismissedUser(dismissedUser);
        dismissed.setCreatedAt(Instant.now());
        
        dismissedSuggestionRepository.save(dismissed);
        
        log.info("Successfully dismissed suggestion");
    }

    @Override
    @Transactional
    public void clearExpiredSuggestions() {
        log.info("Clearing expired suggestions");
        friendSuggestionRepository.deleteExpired(Instant.now());
        log.info("Expired suggestions cleared");
    }

    @Override
    @Transactional
    public void refreshAllSuggestions() {
        log.info("Refreshing suggestions for all active users");
        
        // Get all active users
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(u -> !u.getIsDeleted() && !u.getIsLocked())
                .toList();
        
        int count = 0;
        for (User user : activeUsers) {
            try {
                // Skip if already has valid cache
                boolean hasValidCache = friendSuggestionRepository.existsByUserIdAndExpiresAtAfter(
                    user.getId(), Instant.now()
                );
                
                if (!hasValidCache) {
                    calculateSuggestions(user.getId());
                    count++;
                }
            } catch (Exception e) {
                log.error("Error refreshing suggestions for user: {}", user.getId(), e);
            }
        }
        
        log.info("Refreshed suggestions for {} users", count);
    }

    /**
     * Map FriendSuggestion entity to DTO with enriched data
     */
    private FriendSuggestionDTO mapToDTO(FriendSuggestion suggestion) {
        try {
            User suggestedUser = suggestion.getSuggestedUser();
            UserProfile profile = userProfileRepository.findByUserId(suggestedUser.getId()).orElse(null);
            
            if (profile == null) {
                return null;
            }
            
            // Get avatar
            String avatarUrl = null;
            UserAvatar avatar = userAvatarRepository.findByUserIdAndIsCurrentTrue(suggestedUser.getId());
            if (avatar != null && avatar.getMedia() != null) {
                avatarUrl = avatar.getMedia().getUrl();
            }
            
            // Calculate age
            Integer age = null;
            if (profile.getDateOfBirth() != null) {
                LocalDate birthDate = profile.getDateOfBirth();
                age = Period.between(birthDate, LocalDate.now()).getYears();
            }
            
            return FriendSuggestionDTO.builder()
                    .userId(suggestedUser.getId())
                    .username(suggestedUser.getUsername())
                    .fullName(profile.getFullName())
                    .avatarUrl(avatarUrl)
                    .totalScore(suggestion.getScore())
                    .description(suggestion.getReason())
                    .age(age)
                    .occupation(profile.getOccupation())
                    .city(profile.getCityName())
                    .createdAt(suggestion.getCreatedAt())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error mapping suggestion to DTO", e);
            return null;
        }
    }
}
