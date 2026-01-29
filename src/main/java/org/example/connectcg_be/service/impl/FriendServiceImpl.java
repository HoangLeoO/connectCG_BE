package org.example.connectcg_be.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.FriendDTO;
import org.example.connectcg_be.repository.FriendRepository;
import org.example.connectcg_be.repository.FriendRequestRepository;
import org.example.connectcg_be.service.FriendService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<FriendDTO> getFriends(Integer userId, Integer viewerId, String name, String gender, String cityCode, Pageable pageable) {
        Page<FriendDTO> friends = friendRepository.searchFriends(userId, name, gender, cityCode, pageable);
        
        // Populate relationship status relative to viewerId
        friends.forEach(friend -> {
            Integer targetId = friend.getId();
            if (viewerId.equals(targetId)) {
                friend.setRelationshipStatus("SELF");
            } else if (friendRepository.existsByUserIdAndFriendId(viewerId, targetId)) {
                friend.setRelationshipStatus("FRIEND");
            } else if (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(viewerId, targetId, "PENDING")) {
                friend.setRelationshipStatus("PENDING");
            } else if (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(targetId, viewerId, "PENDING")) {
                friend.setRelationshipStatus("WAITING"); // Or whatever status code for "Request Received"
            } else {
                friend.setRelationshipStatus("STRANGER");
            }
        });
        
        return friends;
    }

    @Override
    @Transactional
    public void unfriend(Integer userId, Integer friendId) {
        if (!friendRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new RuntimeException("You are not friends with this user");
        }
        friendRepository.removeFriendship(userId, friendId);
    }
}
