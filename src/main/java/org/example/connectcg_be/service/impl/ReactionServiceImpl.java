package org.example.connectcg_be.service.impl;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.entity.Post;
import org.example.connectcg_be.entity.Reaction;
import org.example.connectcg_be.entity.ReactionId;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.PostRepository;
import org.example.connectcg_be.repository.ReactionRepository;
import org.example.connectcg_be.repository.UserRepository;
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

            // TODO: Cộng reactCount trong Post nếu cần hiển thị nhanh
            // post.setReactCount(post.getReactCount() + 1);
            // postRepository.save(post);
        }
    }

    @Override
    @Transactional
    public void unreactToPost(Integer postId, Integer userId) {
        ReactionId id = new ReactionId(userId, postId);
        if (reactionRepository.existsById(id)) {
            reactionRepository.deleteById(id);

            // TODO: Trừ reactCount trong Post
        }
    }
}
