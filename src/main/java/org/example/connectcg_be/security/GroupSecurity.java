package org.example.connectcg_be.security;

import org.example.connectcg_be.entity.Group;
import org.example.connectcg_be.entity.GroupMember;
import org.example.connectcg_be.entity.GroupMemberId;
import org.example.connectcg_be.repository.GroupRepository;
import org.example.connectcg_be.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("groupSecurity")
public class GroupSecurity {

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupRepository groupRepository;

    /**
     * Kiểm tra xem người dùng hiện tại có phải là Admin của nhóm này không.
     * 
     * @param groupId ID của nhóm cần kiểm tra
     * @return true nếu là Admin của nhóm, ngược lại false
     */
    public boolean isGroupAdmin(Integer groupId) {
        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null)
            return false;

        // Check if user is the Owner first (from Group entity)
        Optional<Group> group = groupRepository.findById(groupId);
        if (group.isPresent() && userPrincipal.getId().equals(group.get().getOwner().getId())) {
            return true;
        }

        // Check if user has ADMIN role in GroupMember table
        GroupMemberId memberId = new GroupMemberId();
        memberId.setGroupId(groupId);
        memberId.setUserId(userPrincipal.getId());

        Optional<GroupMember> member = groupMemberRepository.findById(memberId);
        return member.isPresent() && "ADMIN".equals(member.get().getRole());
    }

    public boolean isGroupMember(Integer groupId) {
        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null)
            return false;

        GroupMemberId memberId = new GroupMemberId();
        memberId.setGroupId(groupId);
        memberId.setUserId(userPrincipal.getId());

        Optional<GroupMember> member = groupMemberRepository.findById(memberId);
        return member.isPresent() && "ACCEPTED".equals(member.get().getStatus());
    }

    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }
}
