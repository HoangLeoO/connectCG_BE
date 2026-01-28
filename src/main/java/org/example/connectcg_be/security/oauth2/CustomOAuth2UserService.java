package org.example.connectcg_be.security.oauth2;

import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String idSource = oAuth2User.getAttribute("id");

        // Handle case where email is null (e.g. Facebook doesn't return it)
        if (email == null || email.isEmpty()) {
            if (idSource != null) {
                email = idSource + "@facebook.id";
            } else {
                // Determine provider to give a better fallback
                String clientRegId = userRequest.getClientRegistration().getRegistrationId();
                email = "user_" + UUID.randomUUID() + "@" + clientRegId + ".auto";
            }
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Register new user
            user = new User();
            user.setEmail(email);
            // Generate username from email or ID
            if (email.contains("@")) {
                user.setUsername(email.split("@")[0]);
            } else {
                user.setUsername(idSource != null ? idSource : "user" + UUID.randomUUID().toString().substring(0, 8));
            }

            user.setPasswordHash(""); // No password
            user.setRole("USER");
            user.setIsEnabled(true);
            user.setIsDeleted(false);
            user.setIsLocked(false);
            userRepository.save(user);
        }
        // Return DefaultOAuth2User hoặc custom UserPrincipal nếu cần
        return oAuth2User;
    }
}
