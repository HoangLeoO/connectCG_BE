package org.example.connectcg_be.controller;

import org.example.connectcg_be.dto.TypingEventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/typing")
    public void handleTyping(TypingEventDTO event) {
        // Broadcast to the specific chat room topic
        messagingTemplate.convertAndSend("/topic/chat/" + event.getFirebaseRoomKey() + "/typing", event);
    }
}
