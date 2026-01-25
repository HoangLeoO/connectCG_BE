package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.GroupMember;
import org.example.connectcg_be.entity.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {
    List<GroupMember> findAllByIdUserId(Integer userId);

    List<GroupMember> findAllByIdUserIdAndStatus(Integer userId, String status);

    List<GroupMember> findAllByIdGroupId(Integer groupId);

    List<GroupMember> findAllByIdGroupIdAndStatus(Integer groupId, String status);
}
