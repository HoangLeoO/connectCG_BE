package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.FriendRequestRepository;
import org.example.connectcg_be.service.FriendRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FriendRequestServiceImpl implements FriendRequestService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;
}
