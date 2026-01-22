package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.UserAvatarRepository;
import org.example.connectcg_be.service.UserAvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAvatarServiceImpl implements UserAvatarService {

    @Autowired
    private UserAvatarRepository userAvatarRepository;
}
