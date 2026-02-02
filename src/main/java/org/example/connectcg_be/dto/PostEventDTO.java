package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostEventDTO {
    private String action; // "CREATED", "UPDATED", "DELETED"
    private GroupPostDTO post;
    private Integer postId; // Dùng cho DELETED
}
