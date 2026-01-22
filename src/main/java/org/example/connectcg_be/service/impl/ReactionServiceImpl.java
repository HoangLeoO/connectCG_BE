package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.ReactionRepository;
import org.example.connectcg_be.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReactionServiceImpl implements ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;
}
