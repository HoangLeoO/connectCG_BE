package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Chứa: accessToken, tokenType, expiredAt, username
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String username;
    private String role; // Thêm role để FE dễ điều hướng
    private boolean hasProfile;
    public JwtResponse(String accessToken, String refreshToken, String username, String role, boolean hasProfile) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.hasProfile = hasProfile;
    }
}