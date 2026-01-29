package org.example.connectcg_be.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.*;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final UserCoverRepository userCoverRepository;
    private final UserGalleryRepository userGalleryRepository;
    private final UserHobbyRepository userHobbyRepository;
    private final PostRepository postRepository;


    @Override
    public UserProfileDTO getUserProfile(Integer targetUserId, Integer currentUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String relationship = determineRelationship(targetUserId, currentUserId);
        boolean isFriendOrSelf = relationship.equals("SELF") || relationship.equals("FRIEND");

        UserProfile profile = userProfileRepository.findByUserId(targetUserId).orElse(null);

        UserProfileDTO dto = UserProfileDTO.builder()
                .userId(targetUser.getId())
                .username(targetUser.getUsername())
                .role(targetUser.getRole())
                .relationshipStatus(relationship)
                .isFriend(relationship.equals("FRIEND"))
                .build();

        if (profile != null) {
            dto.setFullName(profile.getFullName());
            dto.setGender(profile.getGender());
            dto.setBio(profile.getBio());
            dto.setOccupation(profile.getOccupation());

            if (profile.getCityCode() != null) {
                dto.setCityCode(profile.getCityCode());
                dto.setCityName(profile.getCityName());
            }

            if (isFriendOrSelf) {
                dto.setEmail(targetUser.getEmail());
                dto.setDateOfBirth(profile.getDateOfBirth());
                dto.setMaritalStatus(profile.getMaritalStatus());
                dto.setLookingFor(profile.getLookingFor());
            }
        }

        dto.setCurrentAvatarUrl(getCurrentAvatar(targetUserId));
        dto.setCurrentCoverUrl(getCurrentCover(targetUserId));

        if (isFriendOrSelf) {
            dto.setGallery(getGallery(targetUserId));
        }

        dto.setHobbies(getHobbies(targetUserId));
        dto.setFriendsCount(friendRepository.countByUserId(targetUserId));
        dto.setPostsCount(postRepository.countByAuthorIdAndIsDeletedFalse(targetUserId));

        return dto;
    }

    @Override
    public Page<MemberSearchResponse> searchMembers(
            Integer currentUserId,
            String keyword,
            String gender,
            String cityCode,
            String maritalStatus,
            String lookingFor,
            Pageable pageable
    ) {
        Page<MemberSearchResponse> page = userProfileRepository.searchMembers(
                currentUserId, keyword, gender, cityCode, maritalStatus, lookingFor, pageable
        );

        page.getContent().forEach(dto -> {
            dto.setAvatarUrl(getCurrentAvatar(dto.getUserId()));
        });

        return page;
    }

    private String determineRelationship(Integer targetUserId, Integer currentUserId) {
        if (currentUserId == null) return "STRANGER";
        if (targetUserId.equals(currentUserId)) return "SELF";
        if (friendRepository.existsByUserIdAndFriendId(currentUserId, targetUserId)) return "FRIEND";
        if (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(currentUserId, targetUserId, "PENDING")) return "PENDING";
        return "STRANGER";
    }

    private String getCurrentAvatar(Integer userId) {
        UserAvatar avatar = userAvatarRepository.findByUserIdAndIsCurrentTrue(userId);
        return (avatar != null && avatar.getMedia() != null) ? avatar.getMedia().getUrl() : null;
    }

    private String getCurrentCover(Integer userId) {
        return userCoverRepository.findByUserIdAndIsCurrentTrue(userId)
                .map(uc -> uc.getMedia().getUrl())
                .orElse(null);
    }

    private List<MediaDTO> getGallery(Integer userId) {
        return userGalleryRepository.findByUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(ug -> mapMediaToDTO(ug.getMedia()))
                .collect(Collectors.toList());
    }

    private List<HobbyDTO> getHobbies(Integer userId) {
        return userHobbyRepository.findByUserId(userId)
                .stream()
                .map(uh -> mapHobbyToDTO(uh.getHobby()))
                .collect(Collectors.toList());
    }



    private MediaDTO mapMediaToDTO(Media media) {
        return MediaDTO.builder()
                .id(media.getId())
                .url(media.getUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .type(media.getType())
                .sizeBytes(media.getSizeBytes())
                .uploadedAt(media.getUploadedAt())
                .build();
    }

    private HobbyDTO mapHobbyToDTO(Hobby hobby) {
        return HobbyDTO.builder()
                .id(hobby.getId())
                .code(hobby.getCode())
                .name(hobby.getName())
                .icon(hobby.getIcon())
                .category(hobby.getCategory())
                .build();
    }

    // Implement hàm
    @Override
    @Transactional
    public UserProfileDTO updateProfileInfo(Integer userId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getOccupation() != null) profile.setOccupation(request.getOccupation());
        if (request.getMaritalStatus() != null) profile.setMaritalStatus(request.getMaritalStatus());
        if (request.getLookingFor() != null) profile.setLookingFor(request.getLookingFor());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());

        // Nếu có update city
        if (request.getCityCode() != null) {
             profile.setCityCode(request.getCityCode());
        }
        if (request.getCityName() != null) {
            profile.setCityName(request.getCityName());
        }

        profile.setUpdatedAt(Instant.now());
        userProfileRepository.save(profile);
        return getUserProfile(userId, userId); // Trả về DTO mới nhất
    }
}
