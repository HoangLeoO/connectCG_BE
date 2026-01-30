package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupPostDTO {
    private List<MediaItem> media;
    private Integer id;
    private String content;
    private Instant createdAt;
    private Integer authorId;
    private String authorName;
    private String authorFullName;
    private String authorAvatar;
    private List<String> images;
    private String approvedByFullName;
    private String aiStatus;
    private String visibility;
}
