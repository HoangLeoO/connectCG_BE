package org.example.connectcg_be.service.impl;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.dto.GroupPostDTO;
import org.example.connectcg_be.dto.MediaItem;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.GroupMemberService;
import org.example.connectcg_be.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

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

    @Override
    public List<GroupPostDTO> getNewsfeedPosts(Integer userId) {
        List<Integer> friendIds = friendRepository.findAllFriendIds(userId);
        if (friendIds == null || friendIds.isEmpty())
            friendIds = List.of(-1);
        List<Integer> groupIds = groupMemberService.getAcceptedGroupIds(userId, "ACCEPTED");
        if (groupIds == null || groupIds.isEmpty())
            groupIds = List.of(-1);
        List<Post> posts = postRepository.findNewsfeedPosts(userId, friendIds, groupIds);
        return posts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<GroupPostDTO> getPostsByUserId(Integer userid) {
        List<Post> posts = postRepository.findAllByAuthorIdAndStatusApproved(userid);
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

        Post savedPost = postRepository.save(post);
        attachMediaToPost(savedPost, request.getMediaUrls(), author);
        return savedPost;
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

        Post savedPost = postRepository.save(post);
        attachMediaToPost(savedPost, request.getMediaUrls(), savedPost.getAuthor());
        return savedPost;
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
