package org.example.connectcg_be.controller;

import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.UserPublicKeyService;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat/keys")
public class UserPublicKeyController {

    @Autowired
    private UserPublicKeyService keyService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/public-key")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> savePublicKey(@AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Map<String, String> body) {
        String publicKey = body.get("publicKey");
        if (publicKey == null || publicKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        keyService.savePublicKey(user, publicKey);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public-key/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> getPublicKey(@PathVariable Integer userId) {
        String publicKey = keyService.getPublicKey(userId);
        if (publicKey == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("publicKey", publicKey));
    }
}
