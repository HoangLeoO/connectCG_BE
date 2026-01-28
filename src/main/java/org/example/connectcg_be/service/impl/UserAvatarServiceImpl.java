package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.UserAvatarRepository;
import org.example.connectcg_be.service.UserAvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAvatarServiceImpl implements UserAvatarService {

    @Autowired
    private UserAvatarRepository userAvatarRepository;
    @Autowired
    private org.example.connectcg_be.service.UserService userService;
    @Autowired
    private org.example.connectcg_be.service.MediaService mediaService;
    @Autowired
    private org.example.connectcg_be.service.UserProfileService userProfileService;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public org.example.connectcg_be.dto.UserProfileDTO updateAvatar(Integer userId, String imageUrl) {
        org.example.connectcg_be.entity.User user = userService.findByIdUser(userId);

        // 1. Create Media
        org.example.connectcg_be.entity.Media media = mediaService.createCoverMedia(imageUrl, userId);

        // 2. Deactivate old avatar
        org.example.connectcg_be.entity.UserAvatar currentAvatar = userAvatarRepository
                .findByUserIdAndIsCurrentTrue(userId);
        if (currentAvatar != null) {
            currentAvatar.setIsCurrent(false);
            userAvatarRepository.save(currentAvatar);
        }

        // 3. Create new avatar
        org.example.connectcg_be.entity.UserAvatar newAvatar = new org.example.connectcg_be.entity.UserAvatar();
        newAvatar.setUser(user);
        newAvatar.setMedia(media);
        newAvatar.setIsCurrent(true);
        newAvatar.setSetAt(java.time.Instant.now());
        userAvatarRepository.save(newAvatar);

        // 4. Return updated profile
        return userProfileService.getUserProfile(userId, userId);
    }
}
