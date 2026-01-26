package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.UserProfileDTO;
import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(
            @PathVariable Integer userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        System.out.println(currentUser.getId());
        Integer currentUserId = (currentUser != null) ? currentUser.getId() : null;
        UserProfileDTO profile = userProfileService.getUserProfile(userId, currentUserId);
        return ResponseEntity.ok(profile);
    }
}
