package org.example.connectcg_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MembershipEventDTO {
    private String action; // JOINED, LEFT, APPROVED, REJECTED, KICKED
    private Integer groupId;
    private Integer userId;
    private TungGroupMemberDTO member; // Optional, useful for JOINED/APPROVED
}
