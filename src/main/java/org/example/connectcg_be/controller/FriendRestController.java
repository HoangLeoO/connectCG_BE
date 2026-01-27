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
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendRestController {

    private final FriendService friendService;

    @GetMapping(value = "/{userId}")
    public ResponseEntity<Page<FriendDTO>> getFriendsByUserId(
            @PathVariable(name = "userId") Integer userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer cityId,
            @PageableDefault(size = 2) Pageable pageable) {
        Page<FriendDTO> friends = friendService.getFriends(userId, name, gender, cityId, pageable);
        return ResponseEntity.ok(friends);
    }

    @GetMapping
    public ResponseEntity<Page<FriendDTO>> getMyFriends(
            Authentication authentication,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer cityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(friendService.getFriends(userPrincipal.getId(), name, gender, cityId, pageable));
    }

}
