package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.CreatProfileRequest;
import org.example.connectcg_be.dto.RegisterRequest;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.entity.UserProfile;

public interface AuthService {
    User register(RegisterRequest request);
    UserProfile createProfile(CreatProfileRequest request, String username); // [NEW] created for Onboarding

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}

