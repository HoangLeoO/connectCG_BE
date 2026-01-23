package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.FriendDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendService {
    Page<FriendDTO> getFriends(Integer userId, String name, String gender, Integer cityId, Pageable pageable);
}
