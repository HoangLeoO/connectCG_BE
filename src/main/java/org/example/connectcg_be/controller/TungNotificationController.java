package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.TungNotificationDTO;
import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TungNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<TungNotificationDTO> getMyNotifications(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return notificationService.getMyNotifications(userPrincipal.getId());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable("id") Integer id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }
}
