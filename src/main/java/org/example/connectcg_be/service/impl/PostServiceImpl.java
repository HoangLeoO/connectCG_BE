package org.example.connectcg_be.service.impl;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.dto.*;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.GroupMemberService;
import org.example.connectcg_be.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserAvatarRepository userAvatarRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.example.connectcg_be.service.GeminiService geminiService;

    @Autowired
    private org.example.connectcg_be.repository.GroupRepository groupRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private org.example.connectcg_be.service.NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public List<GroupPostDTO> getPendingPosts(Integer groupId, Integer userId) {
        List<Post> posts = postRepository.findAllByGroupIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(groupId,
                "PENDING");
        return posts.stream()
                .map(post -> convertToDTO(post, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupPostDTO> getApprovedPosts(Integer groupId, Integer userId) {
        List<Post> posts = postRepository.findAllByGroupIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(groupId,
                "APPROVED");
        return posts.stream()
                .map(post -> convertToDTO(post, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupPostDTO> getNewsfeedPosts(Integer userId) {
        List<Integer> friendIds = friendRepository.findAllFriendIds(userId);
        if (friendIds == null || friendIds.isEmpty())
            friendIds = List.of(-1);
        List<Integer> groupIds = groupMemberService.getAcceptedGroupIds(userId, "ACCEPTED");
        if (groupIds == null || groupIds.isEmpty())
            groupIds = List.of(-1);
        List<Post> posts = postRepository.findNewsfeedPosts(userId, friendIds, groupIds);
        return posts.stream()
                .map(post -> convertToDTO(post, userId))
                .collect(Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<GroupPostDTO> getNewsfeedPosts(Integer userId, int page, int size) {
        List<Integer> friendIds = friendRepository.findAllFriendIds(userId);
        if (friendIds == null || friendIds.isEmpty())
            friendIds = List.of(-1);
        List<Integer> groupIds = groupMemberService.getAcceptedGroupIds(userId, "ACCEPTED");
        if (groupIds == null || groupIds.isEmpty())
            groupIds = List.of(-1);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Post> posts = postRepository.findNewsfeedPosts(userId, friendIds, groupIds,
                pageable);
        return posts.map(post -> convertToDTO(post, userId));
    }

    @Override
    public List<GroupPostDTO> getPostsByUserId(Integer userId) {
        List<Post> posts = postRepository.findAllByAuthorIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(userId,
                "APPROVED");
        return posts.stream()
                .map(post -> convertToDTO(post, userId))
                .collect(Collectors.toList());
    }

    private GroupPostDTO convertToDTO(Post post, Integer currentUserId) {
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
        List<PostMedia> mediaList = postMediaRepository.findAllByPostId(post.getId())
                .stream()
                .sorted(Comparator
                        .comparing(pm -> pm.getDisplayOrder() == null ? Integer.MAX_VALUE : pm.getDisplayOrder()))
                .toList();

        List<MediaItem> mediaDto = mediaList.stream().map(pm -> {
            MediaItem item = new MediaItem();
            item.setUrl(pm.getMedia().getUrl());
            item.setType(pm.getMedia().getType());
            item.setDisplayOrder(pm.getDisplayOrder());
            return item;
        }).toList();
        dto.setMedia(mediaDto);
        List<String> images = mediaList.stream()
                .sorted(Comparator
                        .comparing(pm -> pm.getDisplayOrder() == null ? Integer.MAX_VALUE : pm.getDisplayOrder()))
                .map(pm -> pm.getMedia().getUrl())
                .collect(Collectors.toList());
        dto.setImages(images);

        // Moderation fields
        dto.setStatus(post.getStatus()); // APPROVED, PENDING, etc.
        dto.setAiStatus(post.getAiStatus());
        dto.setAiScore(post.getAiScore());
        dto.setAiReason(post.getAiReason());
        dto.setVisibility(post.getVisibility());

        if (post.getApprovedBy() != null) {
            userProfileRepository.findByUserId(post.getApprovedBy().getId()).ifPresent(profile -> {
                dto.setApprovedByFullName(profile.getFullName());
            });
            if (dto.getApprovedByFullName() == null) {
                dto.setApprovedByFullName(post.getApprovedBy().getUsername());
            }
        }
        if (currentUserId != null) {
            org.example.connectcg_be.entity.ReactionId reactionId = new org.example.connectcg_be.entity.ReactionId(
                    currentUserId, post.getId());

            reactionRepository.findById(reactionId).ifPresent(reaction -> {
                dto.setCurrentUserReaction(reaction.getType());
            });
        }

        if (post.getGroup() != null) {
            dto.setGroupId(post.getGroup().getId());
            dto.setGroupName(post.getGroup().getName());

        }

        // Count
        dto.setReactCount((long) (post.getReactCount() != null ? post.getReactCount() : 0));
        dto.setCommentCount(post.getCommentCount() != null ? post.getCommentCount() : 0);

        return dto;
    }

    @Override
    public Page<GroupPostDTO> getPendingHomepagePosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAllByGroupIdIsNullAndStatusAndIsDeletedFalseOrderByCreatedAtDesc("PENDING", pageable)
                .map(post -> convertToDTO(post, null));
    }

    @Override
    public Page<GroupPostDTO> getAuditHomepagePosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository
                .findAllByGroupIdIsNullAndStatusAndAiStatusAndIsDeletedFalseOrderByCreatedAtDesc("APPROVED", "TOXIC",
                        pageable)
                .map(post -> convertToDTO(post, null));
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
        TungNotificationDTO dto = new TungNotificationDTO();
        dto.setContent("Bài viết của bạn đã được phê duyệt.");
        dto.setType("POST_APPROVED");
        dto.setTargetType("POST");
        dto.setTargetId(postId);
        notificationService.sendNotification(dto, post.getAuthor(), admin);

        // Broadcast realtime
        GroupPostDTO postDTO = convertToDTO(post, adminId);
        PostEventDTO event = new PostEventDTO("CREATED", postDTO, post.getId());
        messagingTemplate.convertAndSend("/topic/posts", event);
    }

    @Override
    @Transactional
    public void rejectPost(Integer postId, Integer adminId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        User author = post.getAuthor();

        post.setStatus("REJECTED");
        post.setApprovedBy(admin);
        postRepository.save(post);

        // Send Notification
        TungNotificationDTO dto = new TungNotificationDTO();
        if (post.getGroup() != null) {
            dto.setTargetType("GROUP");
            dto.setTargetId(post.getGroup().getId());
            dto.setContent("Bài viết của bạn trong nhóm '" + post.getGroup().getName() + "' đã bị từ chối.");
        } else {
            dto.setTargetType("POST");
            dto.setTargetId(post.getId());
            dto.setContent("Bài viết của bạn đã bị từ chối.");
        }
        dto.setType("POST_REJECTED");
        notificationService.sendNotification(dto, author, admin);

        // Broadcast realtime delete
        PostEventDTO event = new PostEventDTO("DELETED", null, post.getId());
        messagingTemplate.convertAndSend("/topic/posts", event);

        // Hard delete post record
        postRepository.delete(post);
        postRepository.flush();
    }

    @Override
    @Transactional
    public Post createPost(CreatePostRequest request, boolean skipAiCheck,
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

            // SECURITY: Check if user is an ACCEPTED member or owner/admin of the group
            GroupMemberId memberId = new GroupMemberId();
            memberId.setGroupId(group.getId());
            memberId.setUserId(userId);
            boolean isMember = groupMemberRepository.findById(memberId)
                    .map(m -> "ACCEPTED".equals(m.getStatus()))
                    .orElse(false);

            if (!isMember && !group.getOwner().getId().equals(userId)) {
                throw new RuntimeException("Bạn phải tham gia nhóm mới có thể đăng bài.");
            }

            post.setGroup(group);
        }

        boolean isNewMemberModeration = false;

        // --- ADMIN & OWNER PRIVILEGE ---
        // Website Admins, Group Owners, and Group Admins bypass moderation
        boolean isPrivileged = isPrivilegedUser(author, post.getGroup());

        // MODERATION SCOPE CHECK:
        // 1. Group Posts: Always check (implied Public context)
        // 2. Homepage Posts: Check ONLY if PUBLIC
        boolean isPublic = "PUBLIC".equals(post.getVisibility());
        boolean isGroup = post.getGroup() != null;
        boolean shouldCheckAi = isGroup || isPublic;

        if (!shouldCheckAi) {
            skipAiCheck = true;
        }

        // AI Moderation Logic with Simplified 0.6 threshold
        if (skipAiCheck || isPrivileged) {
            post.setStatus("APPROVED");
            post.setAiStatus(isPrivileged ? "SAFE" : "NOT_CHECKED");
            post.setAiScore(0.0);
            skipAiCheck = true;
        } else {
            AiModerationResult aiResult = geminiService.checkPostContent(request.getContent());
            post.setCheckedAt(Instant.now());
            post.setAiStatus(aiResult.getLabel());
            post.setAiScore(aiResult.getScore());
            post.setAiReason(aiResult.getReason());

            // Unified threshold: < 0.6 is APPROVED, >= 0.6 is PENDING
            // Note: Fail-Safe logic in GeminiService returns 0.9 (PENDING) on error/toxic
            if (aiResult.getScore() < 0.6) {
                post.setStatus("APPROVED");
            } else {
                post.setStatus("PENDING");
            }
        }

        Post savedPost = postRepository.save(post);
        attachMediaToPost(savedPost, request.getMediaUrls(), author);

        if ("APPROVED".equals(savedPost.getStatus())) {
            GroupPostDTO dto = convertToDTO(savedPost, null);
            PostEventDTO event = new PostEventDTO("CREATED", dto, savedPost.getId());
            messagingTemplate.convertAndSend("/topic/posts", event);
        } else if ("PENDING".equals(savedPost.getStatus())) {
            // Broadcast realtime for admins to see the new pending post
            GroupPostDTO dto = convertToDTO(savedPost, null);
            PostEventDTO event = new PostEventDTO("CREATED", dto, savedPost.getId());
            messagingTemplate.convertAndSend("/topic/posts", event);

            TungNotificationDTO notifDto = new TungNotificationDTO();
            notifDto.setContent("Bài viết của bạn đã được gửi và đang chờ quản trị viên phê duyệt.");
            notifDto.setType("POST_PENDING");
            notifDto.setTargetType("POST");
            notifDto.setTargetId(savedPost.getId());
            notificationService.sendNotification(notifDto, author);
        }
        return savedPost;
    }

    @Override
    @Transactional
    public GroupPostDTO createPostAndReturnDTO(CreatePostRequest request, boolean skipAiCheck, Integer userId) {
        Post savedPost = createPost(request, skipAiCheck, userId);
        return convertToDTO(savedPost, userId);
    }

    @Override
    @Transactional
    public Post updatePost(Integer postId, org.example.connectcg_be.dto.CreatePostRequest request, Integer userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        // Check if content or visibility changed to re-trigger moderation
        boolean contentChanged = !post.getContent().equals(request.getContent());
        String oldVisibility = post.getVisibility();
        boolean visibilityChanged = !oldVisibility.equals(request.getVisibility());

        post.setContent(request.getContent());
        post.setVisibility(request.getVisibility());
        post.setUpdatedAt(Instant.now());

        // Trigger check if:
        // 1. Content changed
        // 2. Visibility changed to PUBLIC (might have been private/unchecked before)
        // 3. Visibility changed to GROUP context (if we supported moving posts to
        // groups, but here groupId doesn't change on update usually)
        if (contentChanged || (visibilityChanged && "PUBLIC".equals(request.getVisibility()))) {
            boolean isPrivileged = isPrivilegedUser(post.getAuthor(), post.getGroup());

            // MODERATION SCOPE CHECK:
            // 1. Group Posts: Always check (implied Public context)
            // 2. Homepage Posts: Check ONLY if PUBLIC
            boolean isPublic = "PUBLIC".equals(post.getVisibility());
            boolean isGroup = post.getGroup() != null;
            boolean shouldCheckAi = isGroup || isPublic;

            if (isPrivileged || !shouldCheckAi) {
                // Only auto-approve if we are sure it doesn't need checking
                // If it was already PENDING/TOXIC, we should probably keep it?
                // But if scope says "Don't Check", then it is safe to Approve (e.g. became
                // Private)
                post.setStatus("APPROVED");
                post.setAiStatus(isPrivileged ? "SAFE" : "NOT_CHECKED");
                post.setAiScore(0.0);
            } else {
                // Re-trigger AI Moderation
                // Only check if content changed OR if it was previously unchecked/unknown
                // If content IS SAME, and we already checked it (e.g. was Public SAFE, stayed
                // Public), we could skip.
                // But for safety/simplicity, if it became Public, we check.

                // Opt: If content same and already Safe, skip?
                // Let's just check to be safe and simple.
                AiModerationResult aiResult = geminiService.checkPostContent(request.getContent());
                post.setCheckedAt(Instant.now());
                post.setAiStatus(aiResult.getLabel());
                post.setAiScore(aiResult.getScore());
                post.setAiReason(aiResult.getReason());

                // Reset approver
                post.setApprovedBy(null);

                // Unified 0.6 threshold
                if (aiResult.getScore() < 0.6) {
                    post.setStatus("APPROVED");
                } else {
                    post.setStatus("PENDING");
                }
            }
        }

        Post savedPost = postRepository.save(post);
        attachMediaToPost(savedPost, request.getMediaUrls(), savedPost.getAuthor());

        if ("APPROVED".equals(savedPost.getStatus())) {
            GroupPostDTO dto = convertToDTO(savedPost, null);
            PostEventDTO event = new PostEventDTO("UPDATED", dto, savedPost.getId());
            messagingTemplate.convertAndSend("/topic/posts", event);
        } else if ("PENDING".equals(savedPost.getStatus())) {
            // Broadcast realtime for admins to see the pending update
            GroupPostDTO dto = convertToDTO(savedPost, null);
            PostEventDTO event = new PostEventDTO("UPDATED", dto, savedPost.getId());
            messagingTemplate.convertAndSend("/topic/posts", event);

            TungNotificationDTO notifDto = new TungNotificationDTO();
            notifDto.setContent("Bài viết (chỉnh sửa) của bạn đang chờ kiểm duyệt lại.");
            notifDto.setType("POST_PENDING");
            notifDto.setTargetType("POST");
            notifDto.setTargetId(savedPost.getId());
            notificationService.sendNotification(notifDto, savedPost.getAuthor());
        }
        return savedPost;
    }

    @Transactional
    @Override
    public void deletePost(Integer postId, Integer userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        boolean isAuthor = post.getAuthor().getId().equals(userId);
        boolean isAdmin = "ADMIN".equals(user.getRole());

        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền xóa bài viết này");
        }
        post.setIsDeleted(true);
        postRepository.save(post);
        // Broadcast realtime event
        PostEventDTO event = new PostEventDTO("DELETED", null, postId);
        messagingTemplate.convertAndSend("/topic/posts", event);
    }

    @Override
    public List<Post> getHomepagePostsByStatus(String status) {
        return postRepository.findAllByGroupIdIsNullAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(status);
    }

    @Override
    public org.springframework.data.domain.Page<GroupPostDTO> getHomepagePostsByStatus(String status, int page,
            int size, Integer currentUserId) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Post> posts = postRepository
                .findAllByGroupIdIsNullAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(status, pageable);
        return posts.map(post -> convertToDTO(post, currentUserId));
    }

    private void attachMediaToPost(Post post, List<String> mediaUrls, User uploader) {
        if (mediaUrls == null) {
            return; // không thay đổi media hiện có
        }

        // Xóa liên kết media cũ (nếu có)
        List<PostMedia> existingMedia = postMediaRepository.findAllByPostId(post.getId());
        if (!existingMedia.isEmpty()) {
            postMediaRepository.deleteAll(existingMedia);
        }

        if (mediaUrls.isEmpty()) {
            return; // xóa hết media cũ và không thêm mới
        }

        for (int i = 0; i < mediaUrls.size(); i++) {
            String url = mediaUrls.get(i);
            if (url == null || url.isBlank()) {
                continue;
            }

            Media media = new Media();
            media.setUploader(uploader);
            media.setUrl(url);
            media.setType(detectMediaType(url));
            media.setUploadedAt(Instant.now());
            media.setIsDeleted(false);
            media = mediaRepository.save(media);

            PostMedia postMedia = new PostMedia();
            postMedia.setId(new PostMediaId(post.getId(), media.getId()));
            postMedia.setPost(post);
            postMedia.setMedia(media);
            postMedia.setDisplayOrder(i);
            postMediaRepository.save(postMedia);
        }
    }

    private String detectMediaType(String url) {
        if (url == null) {
            return "IMAGE";
        }
        String lower = url.toLowerCase();
        if (lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.contains("video")) {
            return "VIDEO";
        }
        return "IMAGE";
    }

    private void cleanupPendingPosts(User author, org.example.connectcg_be.entity.Group group) {
        List<Post> pendingPosts;
        if (group != null) {
            pendingPosts = postRepository.findAllByAuthorIdAndGroupIdAndStatusAndIsDeletedFalse(
                    author.getId(), group.getId(), "PENDING");
        } else {
            pendingPosts = postRepository.findAllByAuthorIdAndStatusAndIsDeletedFalse(
                    author.getId(), "PENDING");
        }

        if (pendingPosts.isEmpty())
            return;

        for (Post p : pendingPosts) {
            // Broadcast realtime delete
            PostEventDTO event = new PostEventDTO("DELETED", null, p.getId());
            messagingTemplate.convertAndSend("/topic/posts", event);

            // Hard delete
            postRepository.delete(p);
        }
        postRepository.flush();

        // Send a summary notification
        TungNotificationDTO notifDto = new TungNotificationDTO();
        String groupInfo = (group != null) ? "trong nhóm '" + group.getName() + "' " : "";
        notifDto.setContent(
                "Tất cả các bài viết đang chờ duyệt của bạn " + groupInfo + "đã bị gỡ bỏ do tài khoản bị cấm.");
        notifDto.setType("POST_REJECTED");
        notifDto.setTargetType("USER");
        notifDto.setTargetId(author.getId());
        notificationService.sendNotification(notifDto, author);
    }

    private boolean isPrivilegedUser(User author, Group group) {
        if (author == null)
            return false;

        // Website Admin
        if ("ADMIN".equals(author.getRole()) || "ROLE_ADMIN".equals(author.getRole())) {
            return true;
        }

        // Group Role check
        if (group != null) {
            // Group Owner
            if (group.getOwner() != null && group.getOwner().getId().equals(author.getId())) {
                return true;
            }
            // Group Admin
            GroupMemberId memberId = new GroupMemberId(group.getId(), author.getId());
            return groupMemberRepository.findById(memberId)
                    .map(m -> "ADMIN".equals(m.getRole()))
                    .orElse(false);
        }

        return false;
    }
}
