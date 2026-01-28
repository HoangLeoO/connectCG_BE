package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.UserCoverRepository;
import org.example.connectcg_be.service.UserCoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCoverServiceImpl implements UserCoverService {

    @Autowired
    private UserCoverRepository userCoverRepository;
    @Autowired
    private org.example.connectcg_be.service.UserService userService;
    @Autowired
    private org.example.connectcg_be.service.MediaService mediaService;
    @Autowired
    private org.example.connectcg_be.service.UserProfileService userProfileService;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public org.example.connectcg_be.dto.UserProfileDTO updateCover(Integer userId, String imageUrl) {
        org.example.connectcg_be.entity.User user = userService.findByIdUser(userId);

        // 1. Create Media
        org.example.connectcg_be.entity.Media media = mediaService.createCoverMedia(imageUrl, userId);

        // 2. Deactivate old cover
        java.util.Optional<org.example.connectcg_be.entity.UserCover> currentCoverOpt = userCoverRepository
                .findByUserIdAndIsCurrentTrue(userId);
        if (currentCoverOpt.isPresent()) {
            org.example.connectcg_be.entity.UserCover currentCover = currentCoverOpt.get();
            currentCover.setIsCurrent(false);
            userCoverRepository.save(currentCover);
        }

        // 3. Create new cover
        org.example.connectcg_be.entity.UserCover newCover = new org.example.connectcg_be.entity.UserCover();
        newCover.setUser(user);
        newCover.setMedia(media);
        newCover.setIsCurrent(true);
        newCover.setSetAt(java.time.Instant.now());
        userCoverRepository.save(newCover);

        // 4. Return updated profile
        return userProfileService.getUserProfile(userId, userId);
    }
}
