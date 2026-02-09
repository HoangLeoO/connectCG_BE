package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptDTO {
    private Long roomId;
    private String firebaseRoomKey;
    private Integer userId;
    private Instant lastReadAt;
}
