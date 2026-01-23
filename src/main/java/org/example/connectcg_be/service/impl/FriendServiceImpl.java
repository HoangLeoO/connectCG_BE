package org.example.connectcg_be.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.FriendDTO;
import org.example.connectcg_be.repository.FriendRepository;
import org.example.connectcg_be.service.FriendService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<FriendDTO> getFriends(Integer userId, String name, String gender, Integer cityId, Pageable pageable) {
        return friendRepository.searchFriends(userId, name, gender, cityId, pageable);
    }
}
