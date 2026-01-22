package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.UserHobbyRepository;
import org.example.connectcg_be.service.UserHobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserHobbyServiceImpl implements UserHobbyService {

    @Autowired
    private UserHobbyRepository userHobbyRepository;
}
