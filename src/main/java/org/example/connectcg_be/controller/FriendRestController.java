package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.FriendDTO;
import org.example.connectcg_be.service.FriendService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendRestController {

    private final FriendService friendService;

    @GetMapping(value = "/{userId}")
    public ResponseEntity<Page<FriendDTO>> getFriends(
            @PathVariable(name = "userId") Integer userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer cityId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<FriendDTO> friends = friendService.getFriends(userId, name, gender, cityId, pageable);
        return ResponseEntity.ok(friends);
    }
}
