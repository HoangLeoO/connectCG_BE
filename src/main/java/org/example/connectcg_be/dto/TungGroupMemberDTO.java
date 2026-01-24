package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TungGroupMemberDTO {
    private Integer userId;
    private String username;
    private String avatarUrl;
    private String role;
    private String status;
    private Instant joinedAt;
}
