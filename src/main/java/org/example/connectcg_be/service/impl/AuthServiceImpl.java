package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.CreatProfileRequest;
import org.example.connectcg_be.dto.RegisterRequest;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

        return userRepository.save(user); // CHỈ LƯU USER, KHÔNG TẠO PROFILE
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
}
