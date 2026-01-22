package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.GroupRepository;
import org.example.connectcg_be.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupRepository groupRepository;
}
