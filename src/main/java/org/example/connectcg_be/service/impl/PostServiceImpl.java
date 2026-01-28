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

        // Moderation fields
        dto.setAiStatus(post.getAiStatus());
        dto.setVisibility(post.getVisibility());

        if (post.getApprovedBy() != null) {
            userProfileRepository.findByUserId(post.getApprovedBy().getId()).ifPresent(profile -> {
                dto.setApprovedByFullName(profile.getFullName());
            });
            if (dto.getApprovedByFullName() == null) {
                dto.setApprovedByFullName(post.getApprovedBy().getUsername());
            }
        }

        return dto;
    }

    @Override
    public List<GroupPostDTO> getPendingHomepagePosts() {
        return postRepository.findAllByGroupIdIsNullAndStatusAndIsDeletedFalseOrderByCreatedAtDesc("PENDING")
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<GroupPostDTO> getAuditHomepagePosts() {
        return postRepository
                .findAllByGroupIdIsNullAndStatusAndAiStatusAndIsDeletedFalseOrderByCreatedAtDesc("APPROVED", "TOXIC")
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approvePost(Integer postId, Integer adminId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        post.setStatus("APPROVED");
        post.setApprovedBy(admin);
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);

        // Send Notification
        Notification notification = new Notification();
        notification.setUser(post.getAuthor());
        notification.setActor(admin);
        notification.setType("OTHER"); // Changed from POST_APPROVED to bypass DB check constraint
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        if (post.getGroup() != null) {
            notification.setContent("Bài viết của bạn trong nhóm " + post.getGroup().getName() + " đã được phê duyệt.");
        } else {
            notification.setContent("Bài viết của bạn trên trang chủ đã được phê duyệt.");
        }
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void rejectPost(Integer postId, Integer adminId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Send Notification BEFORE deletion to ensure IDs are valid in memory
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Notification notification = new Notification();
        notification.setUser(post.getAuthor());
        notification.setActor(admin);
        notification.setType("OTHER");

        if (post.getGroup() != null) {
            notification.setTargetType("GROUP");
            notification.setTargetId(post.getGroup().getId());
            notification.setContent("Bài viết của bạn trong nhóm " + post.getGroup().getName() + " đã bị từ chối.");
        } else {
            notification.setTargetType("USER");
            notification.setTargetId(post.getAuthor().getId());
            notification.setContent("Bài viết của bạn trên trang chủ đã bị từ chối và gỡ bỏ.");
        }
        notificationRepository.save(notification);

        // Hard delete the post record
        postRepository.delete(post);
        postRepository.flush(); // Force sync to DB to catch any 500 errors here
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

            if ("SAFE".equals(aiResult)) {
                post.setStatus("APPROVED");
                post.setAiStatus("SAFE");
            } else {
                post.setStatus("PENDING");
                post.setAiStatus(aiResult); // TOXIC or NOT_CHECKED
                post.setAiReason(
                        "SAFE".equals(aiResult) ? null : "Content requires manual review (Flagged or AI Error)");
            }
        }

        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post updatePost(Integer postId, org.example.connectcg_be.dto.CreatePostRequest request, Integer userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        // Check if content changed to re-trigger moderation
        boolean contentChanged = !post.getContent().equals(request.getContent());

        post.setContent(request.getContent());
        post.setVisibility(request.getVisibility());
        post.setUpdatedAt(Instant.now());

        if (contentChanged) {
            // Re-trigger AI Moderation on new content
            String aiResult = geminiService.checkPostContent(request.getContent());
            post.setCheckedAt(Instant.now());
            post.setAiStatus(aiResult);

            // Critical: Reset approver because content is brand new
            post.setApprovedBy(null);

            if ("SAFE".equals(aiResult)) {
                post.setStatus("APPROVED");
            } else {
                // If TOXIC or error occurs, move to PENDING to hide from newsfeed
                post.setStatus("PENDING");
                post.setAiReason("Nội dung đã được thay đổi và cần kiểm duyệt lại");
            }
        }

        return postRepository.save(post);
    }

    @Override
    public List<Post> getHomepagePostsByStatus(String status) {
        return postRepository.findAllByGroupIdIsNullAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(status);
    }
}
