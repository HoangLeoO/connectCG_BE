package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TungNotificationDTO {
    private Integer id;
    private String content;
    private String type;
    private String targetType;
    private Integer targetId;
    private Boolean isRead;
    private Instant createdAt;
    private String actorName;
    private String actorAvatar;
}
