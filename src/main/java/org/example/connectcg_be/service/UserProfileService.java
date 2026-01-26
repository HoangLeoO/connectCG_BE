package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.UserProfileDTO;

public interface UserProfileService {
    UserProfileDTO getUserProfile(Integer targetUserId, Integer currentUserId);
}
