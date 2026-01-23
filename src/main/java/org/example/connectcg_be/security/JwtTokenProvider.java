package org.example.connectcg_be.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    // Lấy secret key từ application.properties
    @Value("${app.jwtSecret:MacDinhLaMotChuoiBiMatRatDaiCanPhaiThayDoiTrongProduction}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs:86400000}") // Mặc định 1 ngày
    private long jwtExpirationInMs;

    // Tạo SecretKey chuẩn cho HS512
    private SecretKey getSigningKey() {
        // app.jwtSecret PHẢI là Base64 hợp lệ
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
        // Nếu lười config Base64, dùng: return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(Integer.toString(userPrincipal.getId())) // Lưu User ID vào subject
                .claim("username", userPrincipal.getUsername())
                .claim("role", userPrincipal.getAuthorities().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    public Integer getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Integer.parseInt(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            log.error("JWT signature không hợp lệ (secret verify không khớp secret đã ký).");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }
}