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
            @RequestParam(required = false) Integer cityId,
            @RequestParam(required = false) String maritalStatus,
            @RequestParam(required = false) String lookingFor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Integer currentUserId = (currentUser != null) ? currentUser.getId() : null;
        if (currentUserId == null) {
             return ResponseEntity.status(401).build();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MemberSearchResponse> result = userProfileService.searchMembers(
                currentUserId, keyword, gender, cityId, maritalStatus, lookingFor, pageable
        );
        return ResponseEntity.ok(result);
    }
}
