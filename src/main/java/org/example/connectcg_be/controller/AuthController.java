package org.example.connectcg_be.controller;

import jakarta.validation.Valid;
import org.example.connectcg_be.dto.JwtResponse;
import org.example.connectcg_be.dto.LoginRequest;
import org.example.connectcg_be.security.JwtTokenProvider;
import org.example.connectcg_be.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // 1. Xác thực username/password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        // 2. Nếu không có lỗi thì set vào Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Lấy thông tin user
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 4. Tạo token
        String jwt = tokenProvider.generateToken(userPrincipal);

        // Return token + info
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                "todo_refresh_token",
                userPrincipal.getUsername(),
                userPrincipal.getAuthorities().toString()));
    }
}