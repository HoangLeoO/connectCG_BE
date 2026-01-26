package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.UserProfileDTO;
import org.example.connectcg_be.entity.User;

import java.util.List;

public interface UserService {
    User findByIdUser(int id);
    List<UserProfileDTO> getAllUser();
}
