package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.FriendDTO;
import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.FriendService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendRestController {

    private final FriendService friendService;

    @GetMapping(value = "/{userId}")
    public ResponseEntity<Page<FriendDTO>> getFriendsByUserId(
            Authentication authentication,
            @PathVariable(name = "userId") Integer userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String cityCode,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Integer viewerId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
             viewerId = ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        
        if (viewerId == null) viewerId = 0;

        Page<FriendDTO> friends = friendService.getFriends(userId, viewerId, name, gender, cityCode, pageable);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/my-friends")
    public ResponseEntity<Page<FriendDTO>> getMyFriends(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String cityCode,
            Pageable pageable) {
        return ResponseEntity.ok(friendService.getFriends(userPrincipal.getId(), userPrincipal.getId(), name, gender, cityCode, pageable));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> unfriend(
            Authentication authentication,
            @PathVariable Integer friendId) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        friendService.unfriend(userPrincipal.getId(), friendId);
        return ResponseEntity.noContent().build();
    }

}
