package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.TungNotificationDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationService {
    List<TungNotificationDTO> getMyNotifications(Integer userId);

    void markAsRead(Integer notificationId);

    void deleteNotification(Integer notificationId);

    @Transactional
    void sendNotification(TungNotificationDTO dto, org.example.connectcg_be.entity.User receiver);

    @Transactional
    void sendNotification(TungNotificationDTO dto, org.example.connectcg_be.entity.User receiver,
            org.example.connectcg_be.entity.User actor);
}
