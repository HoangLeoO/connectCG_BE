package org.example.connectcg_be.service;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.dto.GroupPostDTO;
import java.util.List;

public interface PostService {
        java.util.List<org.example.connectcg_be.dto.GroupPostDTO> getPendingHomepagePosts();

        java.util.List<org.example.connectcg_be.dto.GroupPostDTO> getAuditHomepagePosts();

        org.example.connectcg_be.entity.Post updatePost(Integer postId,
                        org.example.connectcg_be.dto.CreatePostRequest request, Integer userId);

        List<GroupPostDTO> getPendingPosts(Integer groupId, Integer userId);

        List<GroupPostDTO> getApprovedPosts(Integer groupId, Integer userId);

        List<GroupPostDTO> getNewsfeedPosts(Integer userId);

        List<GroupPostDTO> getPostsByUserId(Integer userid);

        void approvePost(Integer postId, Integer adminId);

        void rejectPost(Integer postId, Integer adminId, Boolean manualStrike);

        org.example.connectcg_be.entity.Post createPost(org.example.connectcg_be.dto.CreatePostRequest request,
                        boolean skipAiCheck, Integer userId);

        GroupPostDTO createPostAndReturnDTO(org.example.connectcg_be.dto.CreatePostRequest request,
                        boolean skipAiCheck, Integer userId);

        @Transactional
        void deletePost(Integer postId, Integer userId);

        List<org.example.connectcg_be.entity.Post> getHomepagePostsByStatus(String status);

        org.springframework.data.domain.Page<org.example.connectcg_be.dto.GroupPostDTO> getHomepagePostsByStatus(
                        String status,
                        int page, int size, Integer currentUserId);
}
