package org.example.connectcg_be.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.connectcg_be.dto.UserStatusDTO;
import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.OnlineUserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final OnlineUserService onlineUserService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            UserPrincipal userPrincipal = (UserPrincipal) ((UsernamePasswordAuthenticationToken) user).getPrincipal();
            Integer userId = userPrincipal.getId();
            
            onlineUserService.addUser(userId);
            log.info("User connected: {}", userId);

            // Broadcast online status
            UserStatusDTO statusDTO = new UserStatusDTO(userId, "ONLINE");
            messagingTemplate.convertAndSend("/topic/public/status", statusDTO);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null) {
            UserPrincipal userPrincipal = (UserPrincipal) ((UsernamePasswordAuthenticationToken) user).getPrincipal();
            Integer userId = userPrincipal.getId();
            
            onlineUserService.removeUser(userId);
            log.info("User disconnected: {}", userId);

            // Broadcast offline status
            UserStatusDTO statusDTO = new UserStatusDTO(userId, "OFFLINE");
            messagingTemplate.convertAndSend("/topic/public/status", statusDTO);
        }
    }
}
