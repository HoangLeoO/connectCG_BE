package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentEventDTO {
    private String action; // "CREATED", "DELETED"
    private Integer postId;
    private CommentDTO comment; // Dùng cho CREATED
    private Integer commentId; // Dùng cho DELETED
    private Integer newCommentCount; // Số comment mới sau khi thay đổi
}
