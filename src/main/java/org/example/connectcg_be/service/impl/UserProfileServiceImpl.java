package org.example.connectcg_be.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.*;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

            if (profile.getCity() != null) {
                dto.setCity(mapCityToDTO(profile.getCity()));
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
            Integer cityId,
            String maritalStatus,
            String lookingFor,
            Pageable pageable
    ) {
        Page<MemberSearchResponse> page = userProfileRepository.searchMembers(
                currentUserId, keyword, gender, cityId, maritalStatus, lookingFor, pageable
        );

        page.getContent().forEach(dto -> {
            dto.setAvatarUrl(getCurrentAvatar(dto.getUserId()));
        });

        return page;
    }

    private String determineRelationship(Integer targetUserId, Integer currentUserId) {
        if (currentUserId == null) return "STRANGER";
        if (targetUserId.equals(currentUserId)) return "SELF";
        return friendRepository.existsByUserIdAndFriendId(currentUserId, targetUserId) ? "FRIEND" : "STRANGER";
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

    private CityDTO mapCityToDTO(City city) {
        return CityDTO.builder()
                .id(city.getId())
                .code(city.getCode())
                .name(city.getName())
                .region(city.getRegion())
                .build();
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
}
