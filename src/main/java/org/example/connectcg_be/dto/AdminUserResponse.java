package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserResponse {

    private Integer userId;
    private String username;
    private String email;
    private String role;
    private Boolean isLocked;
}
