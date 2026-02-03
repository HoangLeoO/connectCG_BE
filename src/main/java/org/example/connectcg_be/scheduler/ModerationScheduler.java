package org.example.connectcg_be.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.connectcg_be.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModerationScheduler {

    private final UserRepository userRepository;

    /**
     * Resets violation count for users who haven't had a violation in 30 days.
     * Runs every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetViolations() {
        log.info("Starting scheduled violation reset task...");
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        // Find users with violationCount > 0 and lastViolationAt < thirtyDaysAgo
        userRepository.findAllByViolationCountGreaterThanAndLastViolationAtBefore(0, thirtyDaysAgo)
                .forEach(user -> {
                    log.info("Resetting violations for user: {}", user.getUsername());
                    user.setViolationCount(0);
                    userRepository.save(user);
                });
    }
}
