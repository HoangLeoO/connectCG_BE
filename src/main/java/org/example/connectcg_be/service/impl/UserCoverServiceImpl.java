package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.UserCoverRepository;
import org.example.connectcg_be.service.UserCoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCoverServiceImpl implements UserCoverService {

    @Autowired
    private UserCoverRepository userCoverRepository;
}
