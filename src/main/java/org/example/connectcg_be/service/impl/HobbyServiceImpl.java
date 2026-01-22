package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.HobbyRepository;
import org.example.connectcg_be.service.HobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HobbyServiceImpl implements HobbyService {

    @Autowired
    private HobbyRepository hobbyRepository;
}
