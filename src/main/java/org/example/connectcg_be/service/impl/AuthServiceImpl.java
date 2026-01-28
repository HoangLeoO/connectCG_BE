package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.CreatProfileRequest;
import org.example.connectcg_be.dto.JwtResponse;
import org.example.connectcg_be.dto.RegisterRequest;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.security.JwtTokenProvider;
import org.example.connectcg_be.service.AuthService;
import org.example.connectcg_be.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private MediaRepository mediaRepository; // [NEW]
    @Autowired
    private UserAvatarRepository userAvatarRepository; // [NEW]
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private HobbyRepository hobbyRepository;
    @Autowired
    private UserHobbyRepository userHobbyRepository;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setIsLocked(false);
        user.setIsDeleted(false);
        user.setIsEnabled(false); // [QUAN TRỌNG] : Mặc định chưa kích hoạt

        User savedUser = userRepository.save(user); // CHỈ LƯU USER, KHÔNG TẠO PROFILE

        // [NEW] Tạo Verification Token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, savedUser);
        verificationTokenRepository.save(verificationToken);

        String link = "http://localhost:5173/verify-email?token=" + token;
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f4f4f4; padding: 20px;">
                    <div style="background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
                        <div style="text-align: center; margin-bottom: 20px;">
                            <h1 style="color: #FF5722; margin: 0;">Connect.</h1>
                        </div>
                        <h2 style="color: #333333; margin-top: 0;">Xác thực tài khoản</h2>
                        <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                            Xin chào <strong>%s</strong>,<br><br>
                            Cảm ơn bạn đã đăng ký tài khoản tại Connect. Vui lòng nhấn vào nút bên dưới để kích hoạt tài khoản của bạn.
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #FF5722; color: #ffffff; text-decoration: none; padding: 12px 24px; font-size: 16px; font-weight: bold; border-radius: 5px; display: inline-block;">
                                Xác thực Email
                            </a>
                        </div>
                        <p style="color: #999999; font-size: 14px; text-align: center;">
                            Link này sẽ hết hạn sau 15 phút.
                        </p>
                        <hr style="border: none; border-top: 1px solid #eeeeee; margin: 20px 0;">
                        <p style="color: #aaaaaa; font-size: 12px; text-align: center;">
                            © 2026 Connect. -  All rights reserved.
                        </p>
                    </div>
                </div>
                """.formatted(savedUser.getUsername(), link);
        emailService.sendHtmlMessage(savedUser.getEmail(), "Xác thực tài khoản - Connect", htmlContent);
        return savedUser;
    }

    @Override
    @Transactional
    public UserProfile createProfile(CreatProfileRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (userProfileRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("User already has a profile!");
        }
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName(request.getFullName());
        profile.setOccupation(request.getOccupation());
        profile.setBio(request.getBio());
        // Map Enum UpperCase
        if (request.getGender() != null)
            profile.setGender(request.getGender().toUpperCase());
        if (request.getMaritalStatus() != null)
            profile.setMaritalStatus(request.getMaritalStatus().toUpperCase());
        if (request.getPurpose() != null)
            profile.setLookingFor(request.getPurpose().toUpperCase());
        // Parse Date & City
        if (request.getDateOfBirth() != null) {
            try {
                profile.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
            } catch (Exception e) {
            }
        }
        if (request.getCityId() != null) {
            try {
                cityRepository.findById(Integer.parseInt(request.getCityId())).ifPresent(profile::setCity);
            } catch (Exception e) {
            }
        }

        UserProfile savedProfile = userProfileRepository.save(profile);

        if (request.getHobbyIds() != null && !request.getHobbyIds().isEmpty()) {
            for (Integer hobbyId : request.getHobbyIds()) {
                hobbyRepository.findById(hobbyId).ifPresent(hobby -> {
                    UserHobby userHobby = new UserHobby();
                    UserHobbyId userHobbyId = new UserHobbyId(user.getId(), hobby.getId());
                    userHobby.setId(userHobbyId);

                    userHobby.setUser(user);
                    userHobby.setHobby(hobby);
                    userHobbyRepository.save(userHobby);
                });
            }
        }
        // Lưu Avatar nếu có
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            Media media = new Media();
            media.setUploader(user);
            media.setUrl(request.getAvatarUrl());
            media.setType("IMAGE");
            media.setIsDeleted(false);
            Media savedMedia = mediaRepository.save(media);
            UserAvatar avatar = new UserAvatar();
            avatar.setUser(user);
            avatar.setMedia(savedMedia);
            avatar.setIsCurrent(true);
            userAvatarRepository.save(avatar);
        }
        return savedProfile;

    }

    @Override
    // Hàm Forgot Password
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        tokenRepository.save(myToken);
        String link = "http://localhost:5173/reset-password?token=" + token;
        String htmlContent = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f4f4f4; padding: 20px;">
                        <div style="background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
                            <div style="text-align: center; margin-bottom: 20px;">
                                <h1 style="color: #FF5722; margin: 0;">Connect.</h1>
                            </div>
                            <h2 style="color: #333333; margin-top: 0;">Yêu cầu đặt lại mật khẩu</h2>
                            <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                                Xin chào <strong>%s</strong>,<br><br>
                                Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.
                            </p>
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="background-color: #FF5722; color: #ffffff; text-decoration: none; padding: 12px 24px; font-size: 16px; font-weight: bold; border-radius: 5px; display: inline-block;">
                                    Đặt lại mật khẩu
                                </a>
                            </div>
                            <p style="color: #999999; font-size: 14px; text-align: center;">
                                Link này sẽ hết hạn sau 10 phút.
                            </p>
                            <hr style="border: none; border-top: 1px solid #eeeeee; margin: 20px 0;">
                            <p style="color: #aaaaaa; font-size: 12px; text-align: center;">
                                © 2026 Connect. -  All rights reserved.
                            </p>
                        </div>
                    </div>
                """.formatted(user.getUsername(), link);
        emailService.sendHtmlMessage(email, "Yêu cầu đặt lại mật khẩu - Connect", htmlContent);
    }

    @Override
    // Hàm Reset Password
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token đã hết hạn");
        }
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken); // Mật khẩu đổi xong thì xóa token
    }

    @Override
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token đã hết hạn");
        }
        User user = verificationToken.getUser();
        user.setIsEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }




}
