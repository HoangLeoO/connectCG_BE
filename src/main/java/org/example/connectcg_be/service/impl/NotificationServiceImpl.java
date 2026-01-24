package org.example.connectcg_be.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.TungNotificationDTO;
import org.example.connectcg_be.entity.Notification;
import org.example.connectcg_be.entity.UserAvatar;
import org.example.connectcg_be.repository.NotificationRepository;
import org.example.connectcg_be.repository.UserAvatarRepository;
import org.example.connectcg_be.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserAvatarRepository userAvatarRepository;

    @Override
    public List<TungNotificationDTO> getMyNotifications(Integer userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    private TungNotificationDTO mapToDTO(Notification notification) {
        String actorName = notification.getActor() != null ? notification.getActor().getUsername() : "System";
        String actorAvatarUrl = "https://cdn-icons-png.flaticon.com/512/149/149071.png";

        if (notification.getActor() != null) {
            UserAvatar avatar = userAvatarRepository.findByUserIdAndIsCurrentTrue(notification.getActor().getId());
            if (avatar != null && avatar.getMedia() != null) {
                actorAvatarUrl = avatar.getMedia().getUrl();
            }
        }

        return new TungNotificationDTO(
                notification.getId(),
                notification.getContent(),
                notification.getType(),
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                actorName,
                actorAvatarUrl);
    }
}
