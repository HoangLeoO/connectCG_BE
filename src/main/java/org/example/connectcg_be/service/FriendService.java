package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.FriendDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendService {
    Page<FriendDTO> getFriends(Integer userId, Integer viewerId, String name, String gender, String cityCode, Pageable pageable);

    void unfriend(Integer userId, Integer friendId);
}
