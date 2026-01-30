package org.example.connectcg_be.service;

import org.example.connectcg_be.entity.GroupMember;

import java.util.List;

public interface GroupMemberService {
    GroupMember addGroupMember(GroupMember groupMember);
     List<Integer> getAcceptedGroupIds(Integer userId, String status) ;
}
