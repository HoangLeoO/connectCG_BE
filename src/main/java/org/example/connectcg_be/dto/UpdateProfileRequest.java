package org.example.connectcg_be.dto;

import lombok.Data;

import java.time.LocalDate;
@Data
public class UpdateProfileRequest {
    private String fullName;
    private String bio;
    private String occupation;
    private String maritalStatus;
    private String lookingFor;
    private String gender;
    private LocalDate dateOfBirth;
    private Integer cityId; // Lấy ID thành phố để update
}
