package org.example.connectcg_be.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.UserProfileRepository;
import org.example.connectcg_be.repository.UserRepository;
import org.example.connectcg_be.security.JwtTokenProvider;
import org.example.connectcg_be.security.UserPrincipal;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        // [LOGIC MỚI] Check nếu email bị null
        if (email == null || email.isEmpty()) {
            String idSource = oAuth2User.getAttribute("id"); // Lấy ID Facebook
            if (idSource != null) {
                email = idSource + "@facebook.id"; // Tạo email giả: 12345678@facebook.id
            } else {
                // Trường hợp cực hiếm: không có cả ID -> Báo lỗi
                throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
            }
        }
        User user = userRepository.findByEmail(email).orElseThrow();
        // Generate Token
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String token = tokenProvider.generateToken(userPrincipal);
        boolean hasProfile = userProfileRepository.existsByUserId(user.getId());
        // Redirect về Frontend kèm Token
        String targetUrl = frontendUrl +"/oauth2/redirect?token=" + token + "&hasProfile=" + hasProfile;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
