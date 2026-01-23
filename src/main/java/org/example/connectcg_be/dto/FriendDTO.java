package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendDTO {
    private Integer id;
    private String fullName;
    private String username;
    private String gender;
    private String cityName;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private String occupation;
}
