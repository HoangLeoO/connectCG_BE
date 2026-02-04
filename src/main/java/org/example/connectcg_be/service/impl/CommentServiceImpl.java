package org.example.connectcg_be.service.impl;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.dto.CommentDTO;
import org.example.connectcg_be.dto.CommentEventDTO;
import org.example.connectcg_be.dto.CreateCommentRequest;
import org.example.connectcg_be.entity.Comment;
import org.example.connectcg_be.entity.Post;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.entity.UserAvatar;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserAvatarRepository userAvatarRepository;
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setAuthorId(comment.getAuthor().getId());

        // Lấy tên
        dto.setAuthorName(comment.getAuthor().getUsername());
        userProfileRepository.findByUserId(comment.getAuthor().getId())
                .ifPresent(p -> dto.setAuthorName(p.getFullName()));

        // Lấy avatar
        UserAvatar avatar = userAvatarRepository
                .findByUserIdAndIsCurrentTrue(comment.getAuthor().getId());
        if (avatar != null && avatar.getMedia() != null) {
            dto.setAuthorAvatar(avatar.getMedia().getUrl());
        } else {
            dto.setAuthorAvatar("https://cdn-icons-png.flaticon.com/512/149/149071.png");
        }

        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);

        return dto;
    }

    private int getCommentDepth(Comment comment) {
        int depth = 0;
        Comment current = comment;
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByPostId(Integer postId) {
        List<Comment> allComments = commentRepository
                .findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId);
        Map<Integer, CommentDTO> dtoMap = new HashMap<>();
        List<CommentDTO> rootComments = new ArrayList<>();
        // convert toan bo comment sang dto
        for (Comment c : allComments) {
            CommentDTO dto = convertToDTO(c);
            dtoMap.put(c.getId(), dto);
        }
        // bat dau build tree comment
        for (Comment c : allComments) {
            CommentDTO dto = dtoMap.get(c.getId()); // lay tung comment

            if (c.getParent() == null) { // null => comment cha, cap 1
                // Comment gốc (cấp 1)
                rootComments.add(dto);
            } else {
                // Reply -> Tìm cha và thêm vào danh sách replies
                CommentDTO parentDTO = dtoMap.get(c.getParent().getId());
                if (parentDTO != null) {
                    parentDTO.getReplies().add(dto); // ==> them comment vao danh sach reply cua comment cha
                }
            }
        }

        return rootComments;
    }

    @Override
    @Transactional
    public CommentDTO createComment(Integer postId, Integer userId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(user);
        comment.setContent(request.getContent());
        comment.setCreatedAt(Instant.now());
        comment.setIsDeleted(false);

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy comment cha"));
            // Kiểm tra độ sâu (max 3 cấp)
            int depth = getCommentDepth(parent);
            if (depth >= 2) {
                // Nếu đã ở cấp 3 -> Không cho reply thêm
                // Hoặc có thể gán về cùng cấp với parent
                throw new RuntimeException("Đã đạt giới hạn comment 3 cấp");
            }

            comment.setParent(parent);
        }
        Comment saved = commentRepository.save(comment);

        // Cập nhật comment count của post
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        // Broadcast realtime AFTER commit
        CommentDTO dto = convertToDTO(saved);
        final int newCommentCount = post.getCommentCount();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CommentEventDTO event = new CommentEventDTO("CREATED", postId, dto, saved.getId(), newCommentCount);
                messagingTemplate.convertAndSend("/topic/comments", event);
            }
        });

        return dto;
    }

    @Override
    public void deleteComment(Integer commentId, Integer userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

        // Chỉ cho phép xóa comment của chính mình
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Bạn chỉ có thế xóa comment của chính mình");
        }

        comment.setIsDeleted(true);
        commentRepository.save(comment);

        // Giảm comment count
        Post post = comment.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepository.save(post);

        // Broadcast realtime AFTER commit
        final int postId = post.getId();
        final int newCommentCount = post.getCommentCount();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CommentEventDTO event = new CommentEventDTO("DELETED", postId, null, commentId, newCommentCount);
                messagingTemplate.convertAndSend("/topic/comments", event);
            }
        });
    }

}
