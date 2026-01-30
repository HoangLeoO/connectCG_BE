package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.FriendSuggestionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendSuggestionService {
    
    /**
     * Lấy danh sách gợi ý kết bạn cho user
     * Tự động check cache, nếu hết hạn sẽ tính toán lại
     */
    Page<FriendSuggestionDTO> getSuggestions(Integer userId, Pageable pageable);
    
    /**
     * Tính toán và lưu gợi ý mới cho user
     * Sử dụng thuật toán: Bạn chung (10đ) + Cùng thành phố (5đ)
     */
    void calculateSuggestions(Integer userId);
    
    /**
     * Ẩn/Xóa một gợi ý (user không muốn thấy người này)
     * Sẽ thêm vào bảng dismissed_suggestions để không gợi ý lại
     */
    void dismissSuggestion(Integer userId, Integer dismissedUserId);
    
    /**
     * Dọn dẹp các gợi ý đã hết hạn (chạy scheduled job)
     */
    void clearExpiredSuggestions();
    
    /**
     * Tính toán lại gợi ý cho tất cả users (chạy scheduled job)
     */
    void refreshAllSuggestions();
}
