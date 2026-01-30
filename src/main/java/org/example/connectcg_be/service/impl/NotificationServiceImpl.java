package org.example.connectcg_be.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.TungNotificationDTO;
import org.example.connectcg_be.entity.Notification;
import org.example.connectcg_be.entity.UserAvatar;
import org.example.connectcg_be.repository.NotificationRepository;
import org.example.connectcg_be.repository.UserAvatarRepository;
import org.example.connectcg_be.service.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final org.example.connectcg_be.repository.UserProfileRepository userProfileRepository;
    private final SimpMessagingTemplate messagingTemplate; // 1. Inject cái này

    @Override
    @Transactional(readOnly = true)
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

    @Transactional
    @Override
    public void sendNotification(TungNotificationDTO dto, org.example.connectcg_be.entity.User receiver) {
        sendNotification(dto, receiver, null);
    }

    @Transactional
    @Override
    public void sendNotification(TungNotificationDTO dto, org.example.connectcg_be.entity.User receiver,
            org.example.connectcg_be.entity.User actor) {
        Notification entity = new Notification();
        entity.setUser(receiver);
        entity.setActor(actor);
        entity.setContent(dto.getContent());
        entity.setType(dto.getType());
        entity.setTargetType(dto.getTargetType());
        entity.setTargetId(dto.getTargetId());
        entity.setIsRead(false);
        entity.setCreatedAt(java.time.Instant.now());
        Notification saved = notificationRepository.save(entity);

        dto.setId(saved.getId());
        dto.setCreatedAt(saved.getCreatedAt());
        dto.setIsRead(false);

        // Fetch actor info for real-time WebSocket display
        String actorName = "Hệ thống";
        String actorAvatarUrl = "https://cdn-icons-png.flaticon.com/512/149/149071.png";

        if (actor != null) {
            actorName = userProfileRepository.findByUserId(actor.getId())
                    .map(org.example.connectcg_be.entity.UserProfile::getFullName)
                    .orElse(actor.getUsername());

            org.example.connectcg_be.entity.UserAvatar avatar = userAvatarRepository
                    .findByUserIdAndIsCurrentTrue(actor.getId());
            if (avatar != null && avatar.getMedia() != null) {
                actorAvatarUrl = avatar.getMedia().getUrl();
            }
        }

        dto.setActorName(actorName);
        dto.setActorAvatar(actorAvatarUrl);

        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/notifications",
                dto);
    }

    private TungNotificationDTO mapToDTO(Notification notification) {
        String actorName = notification.getActor() != null ? notification.getActor().getUsername() : "System";
        String actorAvatarUrl = "https://cdn-icons-png.flaticon.com/512/149/149071.png";

        if (notification.getActor() != null) {
            actorName = userProfileRepository.findByUserId(notification.getActor().getId())
                    .map(org.example.connectcg_be.entity.UserProfile::getFullName)
                    .orElse(notification.getActor().getUsername());

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
