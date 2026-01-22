package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.ChatRoomRepository;
import org.example.connectcg_be.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;
}
