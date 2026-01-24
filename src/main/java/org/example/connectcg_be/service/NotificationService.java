package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.TungNotificationDTO;

import java.util.List;

public interface NotificationService {
    List<TungNotificationDTO> getMyNotifications(Integer userId);

    void markAsRead(Integer notificationId);

    void deleteNotification(Integer notificationId);
}
