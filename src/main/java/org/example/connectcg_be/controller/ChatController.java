package org.example.connectcg_be.controller;

import org.example.connectcg_be.dto.ChatRoomDTO;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.UserRepository;
import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Chat Controller handles room metadata
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/direct/{targetUserId}")
    public ResponseEntity<ChatRoomDTO> getOrCreateDirectChat(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Integer targetUserId) {

        User user1 = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        return ResponseEntity.ok(chatRoomService.getOrCreateDirectChat(user1, user2));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ChatRoomDTO>> getMyChatRooms(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(chatRoomService.getUserChatRooms(currentUser.getId()));
    }

    @PostMapping("/group")
    public ResponseEntity<ChatRoomDTO> createGroupChat(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Map<String, Object> request) {

        String name = (String) request.get("name");
        Object memberIdsObj = request.get("memberIds");
        List<Integer> memberIds = (memberIdsObj instanceof List<?>)
                ? ((List<?>) memberIdsObj).stream().map(o -> (Integer) o).collect(Collectors.toList())
                : List.of();

        User creator = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        List<User> members = memberIds.stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Member not found: " + id)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(chatRoomService.createGroupChat(creator, name, members));
    }

    @PutMapping("/{roomId}/name")
    public ResponseEntity<ChatRoomDTO> renameRoom(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long roomId,
            @RequestBody String newName) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove quotes if present from raw string body
        String cleanName = newName.replace("\"", "");
        return ResponseEntity.ok(chatRoomService.renameRoom(roomId, cleanName, user));
    }

    @PutMapping("/{roomId}/avatar")
    public ResponseEntity<ChatRoomDTO> updateAvatar(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long roomId,
            @RequestBody Map<String, String> payload) {

        String url = payload.get("url");
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(chatRoomService.updateAvatar(roomId, url, user));
    }

    @PostMapping("/{roomId}/invite")
    public ResponseEntity<ChatRoomDTO> inviteMembers(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> payload) {

        User inviter = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        // Extract userIds from payload
        Object userIdsObj = payload.get("userIds");
        List<Integer> userIds = (userIdsObj instanceof List<?>)
                ? ((List<?>) userIdsObj).stream().map(o -> (Integer) o).collect(Collectors.toList())
                : List.of();

        if (userIds.isEmpty()) {
            throw new RuntimeException("No users to invite");
        }

        return ResponseEntity.ok(chatRoomService.inviteMembers(roomId, userIds, inviter));
    }
}
