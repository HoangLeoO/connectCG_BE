package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.UserProfileDTO;
import org.example.connectcg_be.entity.User;

import java.util.List;

public interface UserService {
    User findByIdUser(int id);

    List<UserProfileDTO> getAllUser();

    org.springframework.data.domain.Page<UserProfileDTO> getAllUsersPaged(String keyword, String role,
            org.springframework.data.domain.Pageable pageable);

    void updateUserRole(Integer userId, String newRole, Integer actorId);
}
