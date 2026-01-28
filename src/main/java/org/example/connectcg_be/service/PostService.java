package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.GroupPostDTO;
import java.util.List;

public interface PostService {
    java.util.List<org.example.connectcg_be.dto.GroupPostDTO> getPendingHomepagePosts();

    java.util.List<org.example.connectcg_be.dto.GroupPostDTO> getAuditHomepagePosts();

    org.example.connectcg_be.entity.Post updatePost(Integer postId,
            org.example.connectcg_be.dto.CreatePostRequest request, Integer userId);

    List<GroupPostDTO> getPendingPosts(Integer groupId);

    List<GroupPostDTO> getApprovedPosts(Integer groupId);

    void approvePost(Integer postId, Integer adminId);

    void rejectPost(Integer postId, Integer adminId);

    org.example.connectcg_be.entity.Post createPost(org.example.connectcg_be.dto.CreatePostRequest request,
            boolean skipAiCheck, Integer userId);

    List<org.example.connectcg_be.entity.Post> getHomepagePostsByStatus(String status);
}
