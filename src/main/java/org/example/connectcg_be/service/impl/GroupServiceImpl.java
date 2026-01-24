package org.example.connectcg_be.service.impl;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.dto.CreateGroup;
import org.example.connectcg_be.dto.GroupDTO;
import org.example.connectcg_be.dto.TungGroupMemberDTO;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.GroupMemberRepository;
import org.example.connectcg_be.repository.GroupRepository;
import org.example.connectcg_be.repository.NotificationRepository;
import org.example.connectcg_be.service.GroupService;
import org.example.connectcg_be.service.MediaService;
import org.example.connectcg_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MediaService mediaService;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private org.example.connectcg_be.repository.UserAvatarRepository userAvatarRepository;

    @Override
    @Transactional
    public List<GroupDTO> findAllGroups() {
        return groupRepository.findAllByIsDeletedFalse().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Group addGroup(CreateGroup request, int userId) {
        User owner = userService.findByIdUser(userId);

        Media media = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            media = mediaService.createCoverMedia(request.getImage(), userId);
        }

        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setPrivacy(request.getPrivacy());
        group.setOwner(owner);
        group.setCoverMedia(media);
        group.setCreatedAt(Instant.now());
        group.setIsDeleted(false);

        Group savedGroup = groupRepository.save(group);

        GroupMemberId memberId = new GroupMemberId();
        memberId.setGroupId(savedGroup.getId());
        memberId.setUserId(owner.getId());

        GroupMember member = new GroupMember();
        member.setId(memberId);
        member.setGroup(savedGroup);
        member.setUser(owner);
        member.setRole("ADMIN");
        member.setStatus("ACCEPTED");
        member.setJoinedAt(Instant.now());

        groupMemberRepository.save(member);

        return savedGroup;
    }

    @Override
    @Transactional
    public List<GroupDTO> findMyGroups(Integer userId) {
        return groupMemberRepository.findAllByIdUserIdAndStatus(userId, "ACCEPTED").stream().map(member -> {
            Group group = member.getGroup();
            return mapToDTO(group);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GroupDTO> findDiscoverGroups(Integer userId) {
        return groupRepository.findDiscoverGroups(userId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<GroupDTO> searchGroups(String query, Integer userId) {
        return groupRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(query).stream()
                .map(group -> {
                    GroupDTO dto = mapToDTO(group);

                    // Populate membership info for search results
                    GroupMemberId memberId = new GroupMemberId();
                    memberId.setGroupId(group.getId());
                    memberId.setUserId(userId);

                    groupMemberRepository.findById(memberId).ifPresent(member -> {
                        dto.setCurrentUserStatus(member.getStatus());
                        dto.setCurrentUserRole(member.getRole());
                    });

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public GroupDTO findById(Integer id, Integer userId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        GroupDTO dto = mapToDTO(group);

        // Check current user's membership
        GroupMemberId memberId = new GroupMemberId();
        memberId.setGroupId(id);
        memberId.setUserId(userId);

        groupMemberRepository.findById(memberId).ifPresent(member -> {
            dto.setCurrentUserStatus(member.getStatus());
            dto.setCurrentUserRole(member.getRole());
        });

        return dto;
    }

    @Override
    @Transactional
    public Group updateGroup(Integer id, CreateGroup request, Integer userId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You are not the owner of this group");
        }

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setPrivacy(request.getPrivacy());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            Media media = mediaService.createCoverMedia(request.getImage(), userId);
            group.setCoverMedia(media);
        }

        return groupRepository.save(group);
    }

    private GroupDTO mapToDTO(Group group) {
        String ownerName = group.getOwner() != null ? group.getOwner().getUsername() : null;
        Integer ownerId = group.getOwner() != null ? group.getOwner().getId() : null;
        String imageUrl = group.getCoverMedia() != null ? group.getCoverMedia().getUrl() : null;
        Integer coverMediaId = group.getCoverMedia() != null ? group.getCoverMedia().getId() : null;

        return new GroupDTO(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getPrivacy(),
                group.getIsDeleted(),
                group.getCreatedAt(),
                ownerId,
                ownerName,
                coverMediaId,
                imageUrl);
    }

    @Override
    public List<TungGroupMemberDTO> getMembers(Integer groupId, Integer requesterId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if ("PRIVATE".equals(group.getPrivacy())) {
            GroupMemberId id = new GroupMemberId();
            id.setGroupId(groupId);
            id.setUserId(requesterId);
            GroupMember requester = groupMemberRepository.findById(id).orElse(null);
            if (requester == null || !"ACCEPTED".equals(requester.getStatus())) {
                throw new RuntimeException("Cannot view members of a private group unless you are a member");
            }
        }

        return groupMemberRepository.findAllByIdGroupIdAndStatus(groupId, "ACCEPTED").stream().map(member -> {
            org.example.connectcg_be.entity.UserAvatar avatar = userAvatarRepository
                    .findByUserIdAndIsCurrentTrue(member.getUser().getId());
            String avatarUrl = avatar != null ? avatar.getMedia().getUrl()
                    : "https://cdn-icons-png.flaticon.com/512/149/149071.png";

            return new TungGroupMemberDTO(
                    member.getUser().getId(),
                    member.getUser().getUsername(),
                    avatarUrl,
                    member.getRole(),
                    member.getStatus(),
                    member.getJoinedAt());
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void leaveGroup(Integer groupId, Integer userId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (group.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Owner cannot leave the group. Please transfer ownership or delete the group.");
        }

        GroupMemberId memberId = new GroupMemberId();
        memberId.setGroupId(groupId);
        memberId.setUserId(userId);
        groupMemberRepository.deleteById(memberId);
    }

    @Override
    @Transactional
    public void deleteGroup(Integer groupId, Integer userId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only the owner can delete the group");
        }

        group.setIsDeleted(true);
        groupRepository.save(group);
    }

    @Override
    @Transactional
    public void inviteMembers(Integer groupId, List<Integer> userIds, Integer actorId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User actor = userService.findByIdUser(actorId);

        // Security check: Inviter must be an active member of the group
        GroupMemberId actorPk = new GroupMemberId();
        actorPk.setGroupId(groupId);
        actorPk.setUserId(actorId);
        GroupMember actorMember = groupMemberRepository.findById(actorPk).orElse(null);
        if (actorMember == null || !"ACCEPTED".equals(actorMember.getStatus())) {
            throw new RuntimeException("Chỉ thành viên trong nhóm mới có quyền mời người khác.");
        }

        for (Integer userId : userIds) {
            // Check if user exists
            User user = userService.findByIdUser(userId);

            // Check if already a member
            GroupMemberId id = new GroupMemberId();
            id.setGroupId(groupId);
            id.setUserId(userId);

            if (groupMemberRepository.existsById(id)) {
                continue; // Skip if already member
            }

            GroupMember member = new GroupMember();
            member.setId(id);
            member.setGroup(group);
            member.setUser(user);
            member.setRole("MEMBER");
            member.setStatus("PENDING");
            member.setJoinedAt(Instant.now());
            member.setInvitedById(actorId);

            groupMemberRepository.save(member);

            // Create Notification
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setActor(actor);
            notification.setType("GROUP_INVITE");
            notification.setTargetType("GROUP");
            notification.setTargetId(groupId);
            notification.setContent(actor.getUsername() + " đã mời bạn tham gia nhóm " + group.getName());
            notification.setIsRead(false);
            notification.setCreatedAt(Instant.now());

            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void acceptInvitation(Integer groupId, Integer userId) {
        GroupMemberId id = new GroupMemberId();
        id.setGroupId(groupId);
        id.setUserId(userId);

        GroupMember member = groupMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!"PENDING".equals(member.getStatus())) {
            throw new RuntimeException("Invitation is not in PENDING status");
        }

        // Two-stage approval logic
        boolean invitedByAdmin = false;
        if (member.getInvitedById() != null) {
            GroupMemberId inviterPk = new GroupMemberId();
            inviterPk.setGroupId(groupId);
            inviterPk.setUserId(member.getInvitedById());
            GroupMember inviter = groupMemberRepository.findById(inviterPk).orElse(null);
            if (inviter != null && "ADMIN".equals(inviter.getRole())) {
                invitedByAdmin = true;
            }
        }

        boolean isPublicGroup = "PUBLIC".equals(member.getGroup().getPrivacy());

        if (invitedByAdmin || isPublicGroup) {
            member.setStatus("ACCEPTED");
            member.setJoinedAt(Instant.now());
        } else {
            // Requires admin approval (Private Group + Non-admin inviter)
            member.setStatus("REQUESTED");

            // Notify Admin/Owner
            Notification notification = new Notification();
            notification.setUser(member.getGroup().getOwner());
            notification.setActor(member.getUser());
            notification.setType("GROUP_JOIN_REQUEST");
            notification.setTargetType("GROUP");
            notification.setTargetId(groupId);
            notification.setContent(member.getUser().getUsername() + " chấp nhận lời mời tham gia nhóm "
                    + member.getGroup().getName() + ". Vui lòng phê duyệt.");
            notification.setIsRead(false);
            notification.setCreatedAt(Instant.now());
            notificationRepository.save(notification);
        }

        groupMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void declineInvitation(Integer groupId, Integer userId) {
        GroupMemberId id = new GroupMemberId();
        id.setGroupId(groupId);
        id.setUserId(userId);

        GroupMember member = groupMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!"PENDING".equals(member.getStatus())) {
            throw new RuntimeException("Invitation is not in PENDING status");
        }

        groupMemberRepository.delete(member);
    }

    @Override
    public List<GroupDTO> findPendingInvitations(Integer userId) {
        return groupMemberRepository.findAllByIdUserIdAndStatus(userId, "PENDING").stream()
                .map(member -> mapToDTO(member.getGroup()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void joinGroup(Integer groupId, Integer userId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userService.findByIdUser(userId);

        GroupMemberId id = new GroupMemberId();
        id.setGroupId(groupId);
        id.setUserId(userId);

        if (groupMemberRepository.existsById(id)) {
            GroupMember existing = groupMemberRepository.findById(id).get();
            if ("ACCEPTED".equals(existing.getStatus())) {
                throw new RuntimeException("Already a member");
            }
            if ("PENDING".equals(existing.getStatus())) {
                // User has an invitation, follow the same approval rules
                boolean invitedByAdmin = false;
                if (existing.getInvitedById() != null) {
                    GroupMemberId inviterPk = new GroupMemberId();
                    inviterPk.setGroupId(groupId);
                    inviterPk.setUserId(existing.getInvitedById());
                    GroupMember inviter = groupMemberRepository.findById(inviterPk).orElse(null);
                    if (inviter != null && "ADMIN".equals(inviter.getRole())) {
                        invitedByAdmin = true;
                    }
                }

                boolean isPublicGroup = "PUBLIC".equals(group.getPrivacy());

                if (invitedByAdmin || isPublicGroup) {
                    existing.setStatus("ACCEPTED");
                    existing.setJoinedAt(Instant.now());
                } else {
                    // Private Group + Non-admin inviter -> REQUESTED
                    existing.setStatus("REQUESTED");

                    // Notify Admin/Owner
                    Notification notification = new Notification();
                    notification.setUser(group.getOwner());
                    notification.setActor(user);
                    notification.setType("GROUP_JOIN_REQUEST");
                    notification.setTargetType("GROUP");
                    notification.setTargetId(groupId);
                    notification.setContent(user.getUsername() + " chấp nhận lời mời tham gia nhóm " + group.getName()
                            + ". Vui lòng phê duyệt.");
                    notification.setIsRead(false);
                    notification.setCreatedAt(Instant.now());
                    notificationRepository.save(notification);
                }

                groupMemberRepository.save(existing);
                return;
            }
            if ("REQUESTED".equals(existing.getStatus())) {
                throw new RuntimeException("Join request already sent");
            }
        }

        GroupMember member = new GroupMember();
        member.setId(id);
        member.setGroup(group);
        member.setUser(user);
        member.setRole("MEMBER");

        if ("PRIVATE".equals(group.getPrivacy())) {
            member.setStatus("REQUESTED");
        } else {
            member.setStatus("ACCEPTED");
        }
        member.setJoinedAt(Instant.now());

        groupMemberRepository.save(member);

        if ("REQUESTED".equals(member.getStatus())) {
            // Notify Admin
            Notification notification = new Notification();
            notification.setUser(group.getOwner());
            notification.setActor(user);
            notification.setType("GROUP_JOIN_REQUEST");
            notification.setTargetType("GROUP");
            notification.setTargetId(groupId);
            notification.setContent(user.getUsername() + " yêu cầu gia nhập nhóm " + group.getName());
            notification.setIsRead(false);
            notification.setCreatedAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void approveJoinRequest(Integer groupId, Integer targetUserId, Integer adminId) {
        // Check if adminId is actually an admin/owner
        GroupMemberId adminPk = new GroupMemberId();
        adminPk.setGroupId(groupId);
        adminPk.setUserId(adminId);
        GroupMember admin = groupMemberRepository.findById(adminPk)
                .orElseThrow(() -> new RuntimeException("Admin not found in group"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admins can approve requests");
        }

        GroupMemberId targetPk = new GroupMemberId();
        targetPk.setGroupId(groupId);
        targetPk.setUserId(targetUserId);
        GroupMember member = groupMemberRepository.findById(targetPk)
                .orElseThrow(() -> new RuntimeException("Join request not found"));

        if (!"REQUESTED".equals(member.getStatus())) {
            throw new RuntimeException("Member status is not REQUESTED");
        }

        member.setStatus("ACCEPTED");
        member.setJoinedAt(Instant.now());
        groupMemberRepository.save(member);

        // Notify user
        Notification notification = new Notification();
        notification.setUser(member.getUser());
        notification.setActor(admin.getUser());
        notification.setType("GROUP_JOIN_APPROVED");
        notification.setTargetType("GROUP");
        notification.setTargetId(groupId);
        notification.setContent("Yêu cầu gia nhập nhóm " + member.getGroup().getName() + " đã được phê duyệt.");
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void rejectJoinRequest(Integer groupId, Integer targetUserId, Integer adminId) {
        // Validation similar to approve...
        GroupMemberId targetPk = new GroupMemberId();
        targetPk.setGroupId(groupId);
        targetPk.setUserId(targetUserId);
        GroupMember member = groupMemberRepository.findById(targetPk)
                .orElseThrow(() -> new RuntimeException("Join request not found"));

        if (!"REQUESTED".equals(member.getStatus())) {
            throw new RuntimeException("Member status is not REQUESTED");
        }

        User targetUser = member.getUser();
        User actor = userService.findByIdUser(adminId);

        // Create Notification for the rejected member
        Notification notification = new Notification();
        notification.setUser(targetUser);
        notification.setActor(actor);
        notification.setType("GROUP_REJECTED");
        notification.setTargetType("GROUP");
        notification.setTargetId(groupId);
        notification.setContent("Yêu cầu tham gia nhóm " + member.getGroup().getName() + " của bạn đã bị từ chối.");
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notificationRepository.save(notification);

        groupMemberRepository.delete(member);
    }

    @Override
    public List<TungGroupMemberDTO> getPendingJoinRequests(Integer groupId, Integer adminId) {
        return groupMemberRepository.findAllByIdGroupIdAndStatus(groupId, "REQUESTED").stream().map(member -> {
            org.example.connectcg_be.entity.UserAvatar avatar = userAvatarRepository
                    .findByUserIdAndIsCurrentTrue(member.getUser().getId());
            String avatarUrl = avatar != null ? avatar.getMedia().getUrl()
                    : "https://cdn-icons-png.flaticon.com/512/149/149071.png";

            return new TungGroupMemberDTO(
                    member.getUser().getId(),
                    member.getUser().getUsername(),
                    avatarUrl,
                    member.getRole(),
                    member.getStatus(),
                    member.getJoinedAt());
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void kickMember(Integer groupId, Integer targetUserId, Integer adminId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 1. Check if requester is Admin or Owner
        GroupMemberId adminPk = new GroupMemberId();
        adminPk.setGroupId(groupId);
        adminPk.setUserId(adminId);
        GroupMember requester = groupMemberRepository.findById(adminPk).orElse(null);

        boolean isRequesterAdmin = requester != null && "ADMIN".equals(requester.getRole());
        boolean isRequesterOwner = group.getOwner().getId().equals(adminId);

        if (!isRequesterAdmin && !isRequesterOwner) {
            throw new RuntimeException("Only Admins can kick members");
        }

        // 2. Cannot kick self
        if (targetUserId.equals(adminId)) {
            throw new RuntimeException("You cannot kick yourself");
        }

        // 3. Cannot kick the Owner
        if (group.getOwner().getId().equals(targetUserId)) {
            throw new RuntimeException("Cannot kick the group owner");
        }

        User targetUser = userService.findByIdUser(targetUserId);
        User actor = userService.findByIdUser(adminId);

        // Create Notification for the kicked member
        Notification notification = new Notification();
        notification.setUser(targetUser);
        notification.setActor(actor);
        notification.setType("GROUP_KICK");
        notification.setTargetType("GROUP");
        notification.setTargetId(groupId);
        notification.setContent("Bạn đã bị mời ra khỏi nhóm " + group.getName() + " bởi " + actor.getUsername());
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notificationRepository.save(notification);

        // Delete membership
        GroupMemberId targetPk = new GroupMemberId();
        targetPk.setGroupId(groupId);
        targetPk.setUserId(targetUserId);
        groupMemberRepository.deleteById(targetPk);
    }
}
