package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.MemberSearchResponse;
import org.example.connectcg_be.dto.UserProfileDTO;
import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.UserProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final org.example.connectcg_be.service.UserAvatarService userAvatarService;
    private final org.example.connectcg_be.service.UserCoverService userCoverService;

    @PostMapping("/avatar")
    public ResponseEntity<UserProfileDTO> updateAvatar(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody java.util.Map<String, String> payload) {
        String url = payload.get("url");
        if (url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UserProfileDTO updatedProfile = userAvatarService.updateAvatar(currentUser.getId(), url);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/cover")
    public ResponseEntity<UserProfileDTO> updateCover(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody java.util.Map<String, String> payload) {
        String url = payload.get("url");
        if (url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UserProfileDTO updatedProfile = userCoverService.updateCover(currentUser.getId(), url);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(
            @PathVariable Integer userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        System.out.println(currentUser.getId());
        Integer currentUserId = (currentUser != null) ? currentUser.getId() : null;
        UserProfileDTO profile = userProfileService.getUserProfile(userId, currentUserId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MemberSearchResponse>> searchMembers(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) String maritalStatus,
            @RequestParam(required = false) String lookingFor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Integer currentUserId = (currentUser != null) ? currentUser.getId() : null;
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<MemberSearchResponse> result = userProfileService.searchMembers(
                currentUserId, keyword, gender, cityCode, maritalStatus, lookingFor, pageable);
        return ResponseEntity.ok(result);
    }


    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfileInfo(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody org.example.connectcg_be.dto.UpdateProfileRequest request
    ) {
        UserProfileDTO updatedProfile = userProfileService.updateProfileInfo(currentUser.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }
}
