package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.CreateGroup;
import org.example.connectcg_be.dto.GroupDTO;
import org.example.connectcg_be.dto.TungGroupMemberDTO;
import org.example.connectcg_be.entity.Group;

import java.util.List;

public interface GroupService {
    org.springframework.data.domain.Page<GroupDTO> findAllGroups(org.springframework.data.domain.Pageable pageable);

    Group addGroup(CreateGroup request, int userId);

    org.springframework.data.domain.Page<GroupDTO> findMyGroups(Integer userId,
            org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<GroupDTO> findDiscoverGroups(Integer userId,
            org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<GroupDTO> searchGroups(String query, Integer userId,
            org.springframework.data.domain.Pageable pageable);

    GroupDTO findById(Integer id, Integer userId);

    Group updateGroup(Integer id, CreateGroup request, Integer userId);

    List<TungGroupMemberDTO> getMembers(Integer groupId, Integer requesterId);

    void leaveGroup(Integer groupId, Integer userId);

    void deleteGroup(Integer groupId, Integer userId);

    void inviteMembers(Integer groupId, List<Integer> userIds, Integer actorId);

    void acceptInvitation(Integer groupId, Integer userId);

    void declineInvitation(Integer groupId, Integer userId);

    List<GroupDTO> findPendingInvitations(Integer userId);

    void joinGroup(Integer groupId, Integer userId);

    void approveJoinRequest(Integer groupId, Integer targetUserId, Integer adminId);

    void rejectJoinRequest(Integer groupId, Integer targetUserId, Integer adminId);

    List<TungGroupMemberDTO> getPendingJoinRequests(Integer groupId, Integer adminId);

    void kickMember(Integer groupId, Integer targetUserId, Integer adminId);

    void transferOwnershipAndLeave(Integer groupId, Integer newOwnerId, Integer currentOwnerId);

    void updateMemberRole(Integer groupId, Integer targetUserId, String newRole, Integer actorId);
}