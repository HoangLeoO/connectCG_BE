package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.DismissedSuggestionRepository;
import org.example.connectcg_be.service.DismissedSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DismissedSuggestionServiceImpl implements DismissedSuggestionService {

    @Autowired
    private DismissedSuggestionRepository dismissedSuggestionRepository;
}
