package org.example.connectcg_be.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.connectcg_be.service.FriendSuggestionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendSuggestionScheduler {

    private final FriendSuggestionService friendSuggestionService;

    /**
     * Tính toán lại gợi ý cho tất cả users
     * Chạy lúc 2:00 AM hàng ngày
     * Cron: 0 0 2 * * * (giây phút giờ ngày tháng thứ)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void refreshAllSuggestions() {
        log.info("Starting scheduled job: Refresh all friend suggestions");
        try {
            friendSuggestionService.refreshAllSuggestions();
            log.info("Completed scheduled job: Refresh all friend suggestions");
        } catch (Exception e) {
            log.error("Error in scheduled job: Refresh all friend suggestions", e);
        }
    }

    /**
     * Dọn dẹp các gợi ý đã hết hạn
     * Chạy lúc 3:00 AM hàng ngày
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredSuggestions() {
        log.info("Starting scheduled job: Cleanup expired suggestions");
        try {
            friendSuggestionService.clearExpiredSuggestions();
            log.info("Completed scheduled job: Cleanup expired suggestions");
        } catch (Exception e) {
            log.error("Error in scheduled job: Cleanup expired suggestions", e);
        }
    }
}
