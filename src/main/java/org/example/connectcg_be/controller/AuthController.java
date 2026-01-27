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
import org.springframework.web.bind.annotation.*;

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

        try {
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

        } catch (org.springframework.security.authentication.DisabledException e) {
            System.out.println("LOGIN ERROR: DisabledException caught! Account is disabled.");
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email.");
        } catch (org.springframework.security.core.AuthenticationException e) {
            System.out.println("LOGIN ERROR: AuthenticationException caught! Type: " + e.getClass().getName());
            System.out.println("LOGIN ERROR: Message: " + e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body("Sai tên đăng nhập hoặc mật khẩu.");
        }
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("Email sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password updated");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok("Xác thực email thành công! Bạn có thể đăng nhập ngay bây giờ.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}