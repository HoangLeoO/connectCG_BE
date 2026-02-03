package org.example.connectcg_be.service.impl;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.dto.*;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.GroupMemberService;
import org.example.connectcg_be.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
        dto.setAuthorViolationCount(post.getAuthor().getViolationCount());
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
    public List<GroupPostDTO> getPendingHomepagePosts() {
        return postRepository.findAllByGroupIdIsNullAndStatusAndIsDeletedFalseOrderByCreatedAtDesc("PENDING")
                .stream()
                .map(post -> convertToDTO(post, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupPostDTO> getAuditHomepagePosts() {
        return postRepository
                .findAllByGroupIdIsNullAndStatusAndAiStatusAndIsDeletedFalseOrderByCreatedAtDesc("APPROVED", "TOXIC")
                .stream().map(post -> convertToDTO(post, null))
                .collect(Collectors.toList());
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
    public void rejectPost(Integer postId, Integer adminId, Boolean manualStrike) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        User author = post.getAuthor();
        boolean shouldStrike = Boolean.TRUE.equals(manualStrike) || "TOXIC".equals(post.getAiStatus());

        final java.util.concurrent.atomic.AtomicInteger groupStrikesCount = new java.util.concurrent.atomic.AtomicInteger(
                0);
        final java.util.concurrent.atomic.AtomicBoolean isGroupBanned = new java.util.concurrent.atomic.AtomicBoolean(
                false);
        final java.util.concurrent.atomic.AtomicInteger globalStrikesCount = new java.util.concurrent.atomic.AtomicInteger(
                0);
        final java.util.concurrent.atomic.AtomicBoolean isGlobalLocked = new java.util.concurrent.atomic.AtomicBoolean(
                false);
        final java.util.concurrent.atomic.AtomicBoolean isGlobalPermBanned = new java.util.concurrent.atomic.AtomicBoolean(
                false);

        if (shouldStrike) {
            // 1. Group-level strike (if applicable)
            if (post.getGroup() != null) {
                GroupMemberId memberId = new GroupMemberId(post.getGroup().getId(), author.getId());
                groupMemberRepository.findById(memberId).ifPresent(member -> {
                    int currentStrikes = member.getViolationCount() != null ? member.getViolationCount() : 0;
                    int newStrikes = currentStrikes + 1;
                    member.setViolationCount(newStrikes);
                    member.setLastViolationAt(Instant.now());

                    if (newStrikes >= 3) {
                        member.setStatus("BANNED");
                        isGroupBanned.set(true);

                        // Broadcast membership update
                        org.example.connectcg_be.dto.MembershipEventDTO event = new org.example.connectcg_be.dto.MembershipEventDTO(
                                "BANNED", post.getGroup().getId(), author.getId(), null);
                        messagingTemplate.convertAndSend("/topic/groups/membership", event);
                    }
                    groupMemberRepository.save(member);
                    groupStrikesCount.set(newStrikes);
                });
            }

            // 2. Global-level strike
            int currentGlobalStrikes = author.getViolationCount() != null ? author.getViolationCount() : 0;
            int newGlobalStrikes = currentGlobalStrikes + 1;
            author.setViolationCount(newGlobalStrikes);
            author.setLastViolationAt(Instant.now());

            if (newGlobalStrikes == 5) {
                author.setLockedUntil(Instant.now().plus(3, java.time.temporal.ChronoUnit.DAYS));
                author.setIsLocked(true);
                isGlobalLocked.set(true);
            } else if (newGlobalStrikes >= 8) {
                author.setPermanentLocked(true);
                author.setIsLocked(true);
                isGlobalPermBanned.set(true);
            }
            userRepository.save(author);
            globalStrikesCount.set(newGlobalStrikes);
        }

        // Send Notification
        TungNotificationDTO dto = new TungNotificationDTO();

        String strikeReason = Boolean.TRUE.equals(manualStrike) ? "do vi phạm tiêu chuẩn (Admin xác nhận)"
                : "do bị phát hiện nội dung độc hại (AI/Admin xác nhận)";

        if (post.getGroup() != null) {
            dto.setTargetType("GROUP");
            dto.setTargetId(post.getGroup().getId());

            String groupMsg = isGroupBanned.get()
                    ? " Bạn đã bị cấm khỏi nhóm " + post.getGroup().getName() + " do vi phạm " + groupStrikesCount.get()
                            + "/3."
                    : (shouldStrike ? " (Vi phạm " + groupStrikesCount.get() + "/3)" : "");

            dto.setContent("Bài viết của bạn trong nhóm " + post.getGroup().getName() + " đã bị từ chối " + strikeReason
                    + groupMsg);
            dto.setType(isGroupBanned.get() ? "GROUP_BANNED" : (shouldStrike ? "AI_STRIKE_WARNING" : "POST_REJECTED"));
        } else {
            dto.setTargetType("USER");
            dto.setTargetId(author.getId());

            String globalMsg = "";
            if (isGlobalPermBanned.get())
                globalMsg = " Tài khoản của bạn đã bị khóa vĩnh viễn do vi phạm " + globalStrikesCount.get() + " lần.";
            else if (isGlobalLocked.get())
                globalMsg = " Tài khoản của bạn đã bị khóa tạm thời 3 ngày do vi phạm " + globalStrikesCount.get()
                        + "/5.";
            else if (shouldStrike)
                globalMsg = " (Vi phạm hệ thống: " + globalStrikesCount.get() + " gậy).";

            dto.setContent("Bài viết của bạn trên trang chủ đã bị gỡ bỏ " + strikeReason + globalMsg);
            dto.setType(isGlobalPermBanned.get() || isGlobalLocked.get() ? "AI_STRIKE_BANNED" : "POST_REJECTED");
        }

        notificationService.sendNotification(dto, author, admin);

        // Broadcast realtime
        PostEventDTO event = new PostEventDTO("DELETED", null, post.getId());
        messagingTemplate.convertAndSend("/topic/posts", event);

        // Broadcast user update if strikes were changed
        if (Boolean.TRUE.equals(manualStrike)) {
            UserEventDTO userEvent = new UserEventDTO(
                    "UPDATED",
                    author.getId(),
                    author.getViolationCount(),
                    author.getLockedUntil(),
                    author.getPermanentLocked(),
                    author.getIsLocked());
            messagingTemplate.convertAndSend("/topic/users", userEvent);
        }

        // Hard delete the post record
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

        // AI Moderation Logic with Privacy-based thresholds
        if (skipAiCheck) {
            post.setStatus("APPROVED");
            post.setAiStatus("NOT_CHECKED");
            post.setAiScore(0.0);
        } else {
            AiModerationResult aiResult = geminiService.checkPostContent(request.getContent());
            post.setCheckedAt(Instant.now());
            post.setAiStatus(aiResult.getLabel());
            post.setAiScore(aiResult.getScore());
            post.setAiReason(aiResult.getReason());

            boolean needsModeration = false;

            if (post.getGroup() != null) {
                // Group post: Apply privacy-based logic
                String privacy = post.getGroup().getPrivacy();

                if ("PUBLIC".equals(privacy)) {
                    // PUBLIC: Strict threshold (score > 0.4)
                    if (aiResult.getScore() > 0.4) {
                        needsModeration = true;
                    }

                    // Check if member is new (joined within 3 days)
                    GroupMemberId memberId = new GroupMemberId(post.getGroup().getId(), userId);
                    Optional<GroupMember> memberOpt = groupMemberRepository.findById(memberId);
                    if (memberOpt.isPresent() && memberOpt.get().getJoinedAt() != null) {
                        long daysSinceJoined = java.time.Duration.between(
                                memberOpt.get().getJoinedAt(), Instant.now()).toDays();
                        if (daysSinceJoined < 3) {
                            // New member in PUBLIC group: Always moderate
                            needsModeration = true;
                            post.setAiReason(
                                    (post.getAiReason() != null ? post.getAiReason() + " | " : "") +
                                            "Thành viên mới (< 3 ngày) - Cần kiểm duyệt");
                        }
                    }
                } else {
                    // PRIVATE: Relaxed threshold (score > 0.7)
                    if (aiResult.getScore() > 0.7) {
                        needsModeration = true;
                    }
                }
            } else {
                // Homepage post: Use strict threshold like PUBLIC
                if (aiResult.getScore() > 0.4) {
                    needsModeration = true;
                }
            }

            post.setStatus(needsModeration ? "PENDING" : "APPROVED");
        }

        Post savedPost = postRepository.save(post);
        attachMediaToPost(savedPost, request.getMediaUrls(), author);
        if ("APPROVED".equals(savedPost.getStatus())) {
            GroupPostDTO dto = convertToDTO(savedPost, null);
            PostEventDTO event = new PostEventDTO("CREATED", dto, savedPost.getId());
            messagingTemplate.convertAndSend("/topic/posts", event);
        } else if ("PENDING".equals(savedPost.getStatus())) {
            // Notify author about pending status
            TungNotificationDTO dto = new TungNotificationDTO();
            dto.setContent("Bài viết của bạn đang chờ kiểm duyệt do nội dung nhạy cảm theo đánh giá của AI.");
            dto.setType("POST_PENDING");
            dto.setTargetType("POST");
            dto.setTargetId(savedPost.getId());
            notificationService.sendNotification(dto, author);
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

        // Check if content changed to re-trigger moderation
        boolean contentChanged = !post.getContent().equals(request.getContent());

        post.setContent(request.getContent());
        post.setVisibility(request.getVisibility());
        post.setUpdatedAt(Instant.now());

        if (contentChanged) {
            // Re-trigger AI Moderation on new content
            AiModerationResult aiResult = geminiService.checkPostContent(request.getContent());
            post.setCheckedAt(Instant.now());
            post.setAiStatus(aiResult.getLabel());
            post.setAiScore(aiResult.getScore());
            post.setAiReason(aiResult.getReason());

            // Critical: Reset approver because content is brand new
            post.setApprovedBy(null);

            if ("SAFE".equals(aiResult.getLabel())) {
                post.setStatus("APPROVED");
            } else {
                post.setStatus("PENDING");
            }
        }

        Post savedPost = postRepository.save(post);
        attachMediaToPost(savedPost, request.getMediaUrls(), savedPost.getAuthor());

        if ("APPROVED".equals(savedPost.getStatus())) {
            GroupPostDTO dto = convertToDTO(savedPost, null);
            PostEventDTO event = new PostEventDTO("UPDATED", dto, savedPost.getId());
            messagingTemplate.convertAndSend("/topic/posts", event);
        } else if ("PENDING".equals(savedPost.getStatus())) {
            // Notify author about pending status
            TungNotificationDTO dto = new TungNotificationDTO();
            dto.setContent("Bài viết (chỉnh sửa) của bạn đang chờ kiểm duyệt lại.");
            dto.setType("POST_PENDING");
            dto.setTargetType("POST");
            dto.setTargetId(savedPost.getId());
            notificationService.sendNotification(dto, savedPost.getAuthor());
        }
        return savedPost;
    }

    @Transactional
    @Override
    public void deletePost(Integer postId, Integer userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
        if (!post.getAuthor().getId().equals(userId)) {
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
}
