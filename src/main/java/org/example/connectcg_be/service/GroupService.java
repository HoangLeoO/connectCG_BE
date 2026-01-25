package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.CreateGroup;
import org.example.connectcg_be.dto.GroupDTO;
import org.example.connectcg_be.dto.TungGroupMemberDTO;
import org.example.connectcg_be.entity.Group;

import java.util.List;

public interface GroupService {
    List<GroupDTO> findAllGroups();

    Group addGroup(CreateGroup request, int userId);

    List<GroupDTO> findMyGroups(Integer userId);

    List<GroupDTO> findDiscoverGroups(Integer userId);

    List<GroupDTO> searchGroups(String query, Integer userId);

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
}