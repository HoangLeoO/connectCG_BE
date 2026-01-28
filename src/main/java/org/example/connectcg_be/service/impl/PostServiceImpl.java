package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.GroupPostDTO;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserAvatarRepository userAvatarRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.example.connectcg_be.service.GeminiService geminiService;

    @Autowired
    private org.example.connectcg_be.repository.GroupRepository groupRepository;

    @Override
    public List<GroupPostDTO> getPendingPosts(Integer groupId) {
        List<Post> posts = postRepository.findAllByGroupIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(groupId,
                "PENDING");
        return posts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<GroupPostDTO> getApprovedPosts(Integer groupId) {
        List<Post> posts = postRepository.findAllByGroupIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(groupId,
                "APPROVED");
        return posts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private GroupPostDTO convertToDTO(Post post) {
        GroupPostDTO dto = new GroupPostDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setAuthorName(post.getAuthor().getUsername());

        // Get Full Name
        userProfileRepository.findByUserId(post.getAuthor().getId()).ifPresent(profile -> {
            dto.setAuthorFullName(profile.getFullName());
        });
        if (dto.getAuthorFullName() == null) {
            dto.setAuthorFullName(post.getAuthor().getUsername());
        }

        // Get Avatar
        UserAvatar avatar = userAvatarRepository.findByUserIdAndIsCurrentTrue(post.getAuthor().getId());
        if (avatar != null && avatar.getMedia() != null) {
            dto.setAuthorAvatar(avatar.getMedia().getUrl());
        } else {
            dto.setAuthorAvatar("https://cdn-icons-png.flaticon.com/512/149/149071.png");
        }

        // Get Images
        List<PostMedia> mediaList = postMediaRepository.findAllByPostId(post.getId());
        List<String> images = mediaList.stream()
                .map(pm -> pm.getMedia().getUrl())
                .collect(Collectors.toList());
        dto.setImages(images);

        return dto;
    }

    @Override
    @Transactional
    public void approvePost(Integer postId, Integer adminId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setStatus("APPROVED");
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);

        // Send Notification
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Notification notification = new Notification();
        notification.setUser(post.getAuthor());
        notification.setActor(admin);
        notification.setType("OTHER"); // Changed from POST_APPROVED to bypass DB check constraint
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notification.setContent("Bài viết của bạn trong nhóm " + post.getGroup().getName() + " đã được phê duyệt.");
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void rejectPost(Integer postId, Integer adminId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Hard delete the post as requested by user
        postRepository.delete(post);

        // Send Notification
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Notification notification = new Notification();
        notification.setUser(post.getAuthor());
        notification.setActor(admin);
        notification.setType("OTHER"); // Changed from POST_REJECTED to bypass DB check constraint
        notification.setTargetType("GROUP");
        notification.setTargetId(post.getGroup().getId());
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notification.setContent("Bài viết của bạn trong nhóm " + post.getGroup().getName() + " đã bị từ chối.");
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Post createPost(org.example.connectcg_be.dto.CreatePostRequest request, boolean skipAiCheck,
            Integer userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(request.getContent());
        post.setVisibility(request.getVisibility() != null ? request.getVisibility() : "PUBLIC");
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        post.setIsDeleted(false);
        post.setCommentCount(0);
        post.setReactCount(0);
        post.setShareCount(0);

        // Set group if provided
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found"));
            post.setGroup(group);
        }

        // AI Moderation Logic
        if (skipAiCheck) {
            // Skip AI check - approve directly
            post.setStatus("APPROVED");
            post.setAiStatus("NOT_CHECKED");
        } else {
            // Check with Gemini AI
            String aiResult = geminiService.checkPostContent(request.getContent());
            post.setCheckedAt(Instant.now());

            if ("TOXIC".equals(aiResult)) {
                post.setStatus("PENDING");
                post.setAiStatus("TOXIC");
                post.setAiReason("Content flagged by AI for manual review");
            } else {
                post.setStatus("APPROVED");
                post.setAiStatus("SAFE");
            }
        }

        return postRepository.save(post);
    }
}
