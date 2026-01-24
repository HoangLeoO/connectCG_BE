package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    java.util.List<Notification> findAllByUserIdOrderByCreatedAtDesc(Integer userId);
}
