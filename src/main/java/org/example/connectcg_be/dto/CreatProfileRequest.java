package org.example.connectcg_be.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreatProfileRequest {
    private String fullName;
    private String dateOfBirth; // yyyy-MM-dd
    private String gender;
    private String maritalStatus;
    private String purpose; // looking_for
    private String occupation;
    private String cityCode;
    private String cityName;
    private List<Integer> hobbyIds; // [NEW] Danh sách ID sở thích
    private String avatarUrl;
    private String bio;
}
