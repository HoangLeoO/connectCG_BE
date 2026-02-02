package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReactionEventDTO {
    private String action; // "REACTED", "UNREACTED"
    private Integer postId;
    private Integer userId;
    private String reactionType; // "LIKE", "LOVE", "HAHA"...
    private Integer newReactCount; // Số react mới sau khi thay đổi
}
