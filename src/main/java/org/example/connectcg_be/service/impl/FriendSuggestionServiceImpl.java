package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.FriendSuggestionRepository;
import org.example.connectcg_be.service.FriendSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FriendSuggestionServiceImpl implements FriendSuggestionService {

    @Autowired
    private FriendSuggestionRepository friendSuggestionRepository;
}
