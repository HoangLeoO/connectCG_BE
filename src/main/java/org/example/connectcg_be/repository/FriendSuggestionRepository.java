package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.FriendSuggestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FriendSuggestionRepository extends JpaRepository<FriendSuggestion, Integer> {
    
    // Lấy danh sách gợi ý còn hạn cho user
    @Query("SELECT fs FROM FriendSuggestion fs WHERE fs.user.id = :userId AND fs.expiresAt > :now ORDER BY fs.score DESC")
    Page<FriendSuggestion> findActiveByUserId(@Param("userId") Integer userId, @Param("now") Instant now, Pageable pageable);
    
    // Xóa gợi ý cụ thể (khi gửi friend request hoặc dismiss)
    @Modifying
    @Query("DELETE FROM FriendSuggestion fs WHERE fs.user.id = :userId AND fs.suggestedUser.id = :suggestedUserId")
    void deleteByUserIdAndSuggestedUserId(@Param("userId") Integer userId, @Param("suggestedUserId") Integer suggestedUserId);
    
    // Xóa các gợi ý đã hết hạn
    @Modifying
    @Query("DELETE FROM FriendSuggestion fs WHERE fs.expiresAt < :now")
    void deleteExpired(@Param("now") Instant now);
    
    // Kiểm tra xem user đã có suggestions chưa hết hạn chưa
    boolean existsByUserIdAndExpiresAtAfter(Integer userId, Instant now);
}

