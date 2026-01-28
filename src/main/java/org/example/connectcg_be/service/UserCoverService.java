package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.UserProfileDTO;

public interface UserCoverService {
    UserProfileDTO updateCover(Integer userId, String imageUrl);
}
