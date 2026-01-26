package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.UserProfileDTO;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserAvatarRepository userAvatarRepository;

    @Autowired
    private UserCoverRepository userCoverRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Override
    public User findByIdUser(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<UserProfileDTO> getAllUser() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private UserProfileDTO mapToDTO(User user) {
        return mapToDTO(user, null);
    }

    private UserProfileDTO mapToDTO(User user, Integer viewerId) {
        UserProfileDTO dto = new UserProfileDTO();

        // Basic user info
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setIsLocked(user.getIsLocked());

        // Get profile info
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile != null) {
            dto.setFullName(profile.getFullName());
            dto.setDateOfBirth(profile.getDateOfBirth());
            dto.setGender(profile.getGender());
            dto.setBio(profile.getBio());
            dto.setOccupation(profile.getOccupation());
            dto.setMaritalStatus(profile.getMaritalStatus());
            dto.setLookingFor(profile.getLookingFor());

            // City info
            if (profile.getCity() != null) {
                dto.setCity(org.example.connectcg_be.dto.CityDTO.builder()
                        .id(profile.getCity().getId())
                        .name(profile.getCity().getName())
                        .build());
            }
        }

        // Get current avatar
        UserAvatar currentAvatar = userAvatarRepository.findByUserIdAndIsCurrentTrue(user.getId());
        if (currentAvatar != null && currentAvatar.getMedia() != null) {
            dto.setCurrentAvatarUrl(currentAvatar.getMedia().getUrl());
        }

        // Get current cover
        UserCover currentCover = userCoverRepository.findByUserIdAndIsCurrentTrue(user.getId()).orElse(null);
        if (currentCover != null && currentCover.getMedia() != null) {
            dto.setCurrentCoverUrl(currentCover.getMedia().getUrl());
        }

        // Stats
        dto.setPostsCount(postRepository.countByAuthorIdAndIsDeletedFalse(user.getId()));
        dto.setFriendsCount((int) friendRepository.countByUserId(user.getId()));

        // Relationship status (if viewerId is provided)
        if (viewerId != null) {
            if (viewerId.equals(user.getId())) {
                dto.setRelationshipStatus("SELF");
                dto.setIsFriend(false);
            } else {
                boolean isFriend = friendRepository.existsByUserIdAndFriendId(viewerId, user.getId());
                dto.setIsFriend(isFriend);
                dto.setRelationshipStatus(isFriend ? "FRIEND" : "STRANGER");
            }
        }

        return dto;
    }
}
