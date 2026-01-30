package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.entity.GroupMember;
import org.example.connectcg_be.repository.GroupMemberRepository;
import org.example.connectcg_be.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupMemberServiceImpl implements GroupMemberService {

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Override
    public GroupMember addGroupMember(GroupMember groupMember) {
        return groupMemberRepository.save(groupMember);
    }

    @Override
    public List<Integer> getAcceptedGroupIds(Integer userId, String status) {
        return groupMemberRepository.findAllByIdUserIdAndStatus(userId, status)
                .stream()
                .map(gm -> gm.getId().getGroupId())
                .toList();
    }
}
