package org.example.connectcg_be.service;

import org.springframework.stereotype.Service;

@Service
public interface ReactionService {
    void reactToPost(Integer postId, Integer userId, String type);

    void unreactToPost(Integer postId, Integer userId);
}
