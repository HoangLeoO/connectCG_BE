package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.MemberSearchResponse;
import org.example.connectcg_be.dto.UserProfileDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserProfileService {
    UserProfileDTO getUserProfile(Integer targetUserId, Integer currentUserId);
    
    Page<MemberSearchResponse> searchMembers(
            Integer currentUserId,
            String keyword,
            String gender,
            Integer cityId,
            String maritalStatus,
            String lookingFor,
            Pageable pageable
    );
}
