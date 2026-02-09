package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingEventDTO {
    private String firebaseRoomKey;
    private Integer userId;
    private String fullName;
    private boolean typing;
}
