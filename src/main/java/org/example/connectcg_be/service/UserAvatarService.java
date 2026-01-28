package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.UserProfileDTO;

public interface UserAvatarService {
    UserProfileDTO updateAvatar(Integer userId, String imageUrl);
}
