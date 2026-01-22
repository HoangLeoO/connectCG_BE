package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.ChatRoomMemberRepository;
import org.example.connectcg_be.service.ChatRoomMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomMemberServiceImpl implements ChatRoomMemberService {

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;
}
