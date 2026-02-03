package org.example.connectcg_be.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiModerationResult {
    private Double score;
    private String label;
    private String reason;
}
