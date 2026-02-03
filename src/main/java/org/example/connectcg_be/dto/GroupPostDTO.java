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

    private Integer groupId;
    private String groupName;

    private String content;
    private Instant createdAt;
    private Integer authorId;
    private String authorName;
    private String authorFullName;
    private String authorAvatar;
    private List<String> images;
    private String approvedByFullName;
    private String status; // Added for frontend to check if post is APPROVED/PENDING
    private String aiStatus;
    private Double aiScore;
    private String aiReason;
    private String visibility;
    private String currentUserReaction;
    private Long reactCount;
    private Integer commentCount;
    private Integer authorViolationCount;
}
