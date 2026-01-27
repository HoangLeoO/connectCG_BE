package org.example.connectcg_be.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.AdminUserResponse;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.UserRepository;
import org.example.connectcg_be.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    public Page<AdminUserResponse> getAllUsers(
            int page,
            int size,
            String keyword,
            String role
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> users = userRepository.adminSearchUsers(
                isBlank(keyword) ? null : keyword,
                isBlank(role) ? null : role,
                pageable
        );

        return users.map(this::toResponse);
    }

    public void updateUserRole(Integer userId, String role) {
        User user = getUser(userId);
        user.setRole(role);
        userRepository.save(user);
    }

    public void toggleLock(Integer userId) {
        User user = getUser(userId);
        user.setIsLocked(!Boolean.TRUE.equals(user.getIsLocked()));
        userRepository.save(user);
    }

    public void softDelete(Integer userId) {
        User user = getUser(userId);
        user.setIsDeleted(true);
        userRepository.save(user);
    }

    // ===================== PRIVATE =====================

    private User getUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private AdminUserResponse toResponse(User u) {
        return new AdminUserResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole(),
                Boolean.TRUE.equals(u.getIsLocked())
        );
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

