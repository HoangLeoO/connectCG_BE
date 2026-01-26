package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestDTO {
    private Integer requestId;
    private Integer senderId;
    private String senderUsername;
    private String senderFullName;
    private String senderAvatarUrl;
    private Instant createdAt;
}
