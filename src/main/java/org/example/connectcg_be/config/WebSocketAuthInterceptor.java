package org.example.connectcg_be.config;

import org.example.connectcg_be.security.CustomUserDetailsService;
import org.example.connectcg_be.security.JwtTokenProvider;
import org.example.connectcg_be.security.UserPrincipal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;

public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() == null) {
                UsernamePasswordAuthenticationToken auth = resolveUser(accessor);
                if (auth != null) {
                    accessor.setUser(auth);
                }
            }
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken resolveUser(StompHeaderAccessor accessor) {
        String token = resolveTokenFromHeaders(accessor);
        if (token != null && tokenProvider.validateToken(token)) {
            Integer userId = tokenProvider.getUserIdFromJWT(token);
            UserPrincipal user = (UserPrincipal) userDetailsService.loadUserById(userId);
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }

        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs != null) {
            Object userAttr = attrs.get("user");
            if (userAttr instanceof UsernamePasswordAuthenticationToken auth) {
                return auth;
            }
        }

        return null;
    }

    private String resolveTokenFromHeaders(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null) {
            authHeader = accessor.getFirstNativeHeader("authorization");
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        String token = accessor.getFirstNativeHeader("token");
        if (token == null) {
            token = accessor.getFirstNativeHeader("access_token");
        }
        return (token != null && !token.isBlank()) ? token : null;
    }
}
