package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Integer userId;
    private String username;
    private String email;
    private String role;
    private Boolean isLocked; // Status account

    // Profile info
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String bio;
    private String occupation;
    private String maritalStatus;
    private String lookingFor;

    // Location
    private CityDTO city;

    // Media
    private String currentAvatarUrl;
    private String currentCoverUrl;
    private List<MediaDTO> gallery;

    // Hobbies
    private List<HobbyDTO> hobbies;

    // Stats
    private Integer friendsCount;
    private Integer postsCount;

    // Relationship status
    private String relationshipStatus; // SELF, FRIEND, STRANGER
    private Boolean isFriend;
}
