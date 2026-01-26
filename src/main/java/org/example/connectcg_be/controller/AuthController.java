package org.example.connectcg_be.controller;

import jakarta.validation.Valid;
import org.example.connectcg_be.dto.CreatProfileRequest;
import org.example.connectcg_be.dto.JwtResponse;
import org.example.connectcg_be.dto.LoginRequest;
import org.example.connectcg_be.dto.RegisterRequest;
import org.example.connectcg_be.repository.UserProfileRepository;
import org.example.connectcg_be.security.JwtTokenProvider;
import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.AuthService;
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
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserProfileRepository userProfileRepository;

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

        boolean hasProfile = userProfileRepository.existsByUserId(userPrincipal.getId());

        // 4. Tạo token
        String jwt = tokenProvider.generateToken(userPrincipal);

        // Return token + info
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                "todo_refresh_token",
                userPrincipal.getUsername(),
                userPrincipal.getAuthorities().toString(),
                hasProfile));
    }

    // [NEW] API Đăng ký Account (Step 1)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            authService.register(signUpRequest);
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [NEW] API Tạo Profile (Step 2 - Onboarding)
    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(@Valid @RequestBody CreatProfileRequest profileRequest) {
        try {
            // Lấy username từ Token đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            authService.createProfile(profileRequest, currentUsername);
            return ResponseEntity.ok("Profile created successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}