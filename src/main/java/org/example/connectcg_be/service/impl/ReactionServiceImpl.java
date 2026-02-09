package org.example.connectcg_be.service.impl;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.dto.ReactionEventDTO;
import org.example.connectcg_be.entity.Notification;
import org.example.connectcg_be.entity.Post;
import org.example.connectcg_be.entity.Reaction;
import org.example.connectcg_be.entity.ReactionId;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.PostRepository;
import org.example.connectcg_be.repository.ReactionRepository;
import org.example.connectcg_be.repository.UserRepository;
import org.example.connectcg_be.service.NotificationService;
import org.example.connectcg_be.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReactionServiceImpl implements ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private org.example.connectcg_be.repository.UserProfileRepository userProfileRepository;
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void reactToPost(Integer postId, Integer userId, String type) {
        // 1. Tạo Composite Key
        ReactionId id = new ReactionId(userId, postId);
        // 2. Kiểm tra xem đã tồn tại chưa
        Optional<Reaction> existingReaction = reactionRepository.findById(id);
        if (existingReaction.isPresent()) {
            // Update reaction type (VD: LIKE -> LOVE)
            Reaction reaction = existingReaction.get();
            reaction.setType(type);
            reactionRepository.save(reaction);
        } else {
            // Tạo mới
            // Lấy Post và User proxy (getReference để tối ưu query)
            Post post = postRepository.getReferenceById(postId);
            User user = userRepository.getReferenceById(userId);
            Reaction reaction = new Reaction();
            reaction.setId(id);
            reaction.setPost(post);
            reaction.setUser(user);
            reaction.setType(type);

            reactionRepository.save(reaction);

            // Cộng reactCount trong Post
            post.setReactCount((post.getReactCount() != null ? post.getReactCount() : 0) + 1);
            postRepository.save(post);

            // Gửi thông báo cho chủ bài viết
            if (!userId.equals(post.getAuthor().getId())) {
                Notification notification = new Notification();
                notification.setUser(post.getAuthor());
                notification.setActor(user);
                notification.setType("POST_REACTION");
                notification.setTargetType("POST");
                notification.setTargetId(postId);
                notification.setIsRead(false);

                String actorName = userProfileRepository.findByUserId(userId)
                        .map(org.example.connectcg_be.entity.UserProfile::getFullName)
                        .orElse(user.getUsername());
                notification.setContent(actorName + " đã bày tỏ cảm xúc về bài viết của bạn.");

                notificationService.sendNotification(notification);
            }
        }

        // Broadcast realtime
        Post updatedPost = postRepository.findById(postId).orElse(null);
        int newCount = updatedPost != null && updatedPost.getReactCount() != null
                ? updatedPost.getReactCount()
                : 0;
        ReactionEventDTO event = new ReactionEventDTO("REACTED", postId, userId, type, newCount);
        messagingTemplate.convertAndSend("/topic/reactions", event);
    }

    @Override
    @Transactional
    public void unreactToPost(Integer postId, Integer userId) {
        ReactionId id = new ReactionId(userId, postId);
        if (reactionRepository.existsById(id)) {
            reactionRepository.deleteById(id);

            // Trừ reactCount trong Post
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null && post.getReactCount() != null && post.getReactCount() > 0) {
                post.setReactCount(post.getReactCount() - 1);
                postRepository.save(post);
            }

            // Broadcast realtime
            Post updatedPost = postRepository.findById(postId).orElse(null);
            int newCount = updatedPost != null && updatedPost.getReactCount() != null
                    ? updatedPost.getReactCount()
                    : 0;
            ReactionEventDTO event = new ReactionEventDTO("UNREACTED", postId, userId, null, newCount);
            messagingTemplate.convertAndSend("/topic/reactions", event);
        }
    }
}
