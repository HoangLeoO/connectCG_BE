package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Integer id;
    private String content;
    private Instant createdAt;

    // Thông tin người comment
    private Integer authorId;
    private String authorName;
    private String authorAvatar;

    // ID của comment cha (null nếu là root)
    private Integer parentId;

    // Danh sách reply (comment con) - Dùng cho cấu trúc cây
    private List<CommentDTO> replies = new ArrayList<>();
}
