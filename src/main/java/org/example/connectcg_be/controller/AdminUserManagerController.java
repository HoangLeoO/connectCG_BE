package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.UserProfileDTO;
import org.example.connectcg_be.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.example.connectcg_be.security.UserPrincipal;

@RestController
@RequestMapping("/api/admin-user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminUserManagerController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<UserProfileDTO>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsersPaged(keyword, role, pageable));
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateRole(@PathVariable Integer userId,
            @RequestBody java.util.Map<String, String> body, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String newRole = body.get("role");
        userService.updateUserRole(userId, newRole, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleLock(
            @PathVariable Integer userId,
            Authentication authentication
    ) {
        UserPrincipal admin = (UserPrincipal) authentication.getPrincipal();
        userService.toggleLockUser(userId, admin.getId());
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{userId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Integer userId,
            Authentication authentication
    ) {
        UserPrincipal admin = (UserPrincipal) authentication.getPrincipal();
        userService.softDeleteUser(userId, admin.getId());
        return ResponseEntity.ok().build();
    }

}
