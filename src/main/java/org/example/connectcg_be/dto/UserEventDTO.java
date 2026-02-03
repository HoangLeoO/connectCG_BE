package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEventDTO {
    private String action; // "UPDATED"
    private Integer userId;
    private Instant lockedUntil;
    private Boolean permanentLocked;
    private Boolean isLocked;
}
