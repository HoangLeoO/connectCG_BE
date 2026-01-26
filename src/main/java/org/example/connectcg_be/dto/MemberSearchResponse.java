package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSearchResponse {
    private Integer userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String cityName;
    private String gender;
    private String maritalStatus;
    private String lookingFor;
    private Boolean isFriend;
    private Long mutualFriends;

    // Constructor for JPQL projection
    public MemberSearchResponse(Integer userId, String username, String fullName, String cityName, String gender, String maritalStatus, String lookingFor, Boolean isFriend) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.cityName = cityName;
        this.gender = gender;
        this.maritalStatus = maritalStatus;
        this.lookingFor = lookingFor;
        this.isFriend = isFriend;
    }
}
