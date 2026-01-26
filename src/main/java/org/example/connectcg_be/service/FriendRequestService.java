package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.FriendRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendRequestService {
    Page<FriendRequestDTO> getPendingRequests(Integer userId, Pageable pageable);
    void acceptFriendRequest(Integer requestId, Integer userId);
    void rejectFriendRequest(Integer requestId, Integer userId);
    void sendFriendRequest(Integer senderId, Integer receiverId);
}
