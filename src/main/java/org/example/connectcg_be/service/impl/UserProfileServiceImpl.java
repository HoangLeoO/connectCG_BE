package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.UserProfileRepository;
import org.example.connectcg_be.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;
}
