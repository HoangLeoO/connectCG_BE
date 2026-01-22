package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.NotificationRepository;
import org.example.connectcg_be.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
}
