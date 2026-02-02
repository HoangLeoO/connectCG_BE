package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private String type;
    private String name;
    private String avatarUrl;
    private String firebaseRoomKey;
    private Integer otherParticipantId;
    private List<ChatMemberDTO> members;
    private Instant lastMessageAt;
    private Instant createdAt;
    private Integer unreadCount;
    private Instant clientClearedAt;
    private String currentUserRole;
}
