package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.FriendRepository;
import org.example.connectcg_be.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendRepository friendRepository;
}
