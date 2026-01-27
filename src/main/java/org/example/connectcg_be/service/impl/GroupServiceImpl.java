package org.example.connectcg_be.service.impl;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.dto.CreateGroup;
import org.example.connectcg_be.dto.GroupDTO;
import org.example.connectcg_be.dto.TungGroupMemberDTO;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.GroupMemberRepository;
import org.example.connectcg_be.repository.GroupRepository;
import org.example.connectcg_be.repository.PostRepository;
import org.example.connectcg_be.service.GroupService;
import org.example.connectcg_be.service.MediaService;
import org.example.connectcg_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    // Fields already defined in replacement chunk 1
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MediaService mediaService;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private org.example.connectcg_be.repository.UserAvatarRepository userAvatarRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private org.example.connectcg_be.repository.UserProfileRepository userProfileRepository;
    @Autowired
    private org.example.connectcg_be.repository.NotificationRepository notificationRepository;

    @Override
    @Transactional
    public List<GroupDTO> findAllGroups() {
        return groupRepository.findAllByIsDeletedFalse().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private GroupDTO mapToDTO(Group group) {
        return mapToDTO(group, null);
    }

    private GroupDTO mapToDTO(Group group, Integer userId) {
        GroupDTO dto = mapToBasicDTO(group);

        // Membership info
        if (userId != null) {
            GroupMemberId memberId = new GroupMemberId();
            memberId.setGroupId(group.getId());
            memberId.setUserId(userId);
            groupMemberRepository.findById(memberId).ifPresent(member -> {
                dto.setCurrentUserStatus(member.getStatus());
                dto.setCurrentUserRole(member.getRole());
            });
        }

        return dto;
    }

    private GroupDTO mapToBasicDTO(Group group) {
        String ownerName = group.getOwner() != null ? group.getOwner().getUsername() : null;
        Integer ownerId = group.getOwner() != null ? group.getOwner().getId() : null;
        String ownerFullName = ownerName;
        if (ownerId != null) {
            ownerFullName = userProfileRepository.findByUserId(ownerId)
                    .map(UserProfile::getFullName)
                    .orElse(ownerName);
        }
        String imageUrl = group.getCoverMedia() != null ? group.getCoverMedia().getUrl() : null;
        Integer coverMediaId = group.getCoverMedia() != null ? group.getCoverMedia().getId() : null;

        GroupDTO dto = new GroupDTO(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getPrivacy(),
                group.getIsDeleted(),
                group.getCreatedAt(),
                ownerId,
                ownerName,
                ownerFullName,
                coverMediaId,
                imageUrl);

        // Populate counts
        dto.setPendingRequestsCount(groupMemberRepository.countByIdGroupIdAndStatus(group.getId(), "REQUESTED"));
        dto.setPendingPostsCount(postRepository.countByGroupIdAndStatus(group.getId(), "PENDING"));
        dto.setMemberCount(groupMemberRepository.countByIdGroupIdAndStatus(group.getId(), "ACCEPTED"));

        return dto;
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
        return groupMemberRepository.findAllByIdUserIdAndStatus(userId, "ACCEPTED").stream()
                .filter(member -> !member.getGroup().getIsDeleted())
                .map(member -> {
                    GroupDTO dto = mapToDTO(member.getGroup(), userId);
                    dto.setCurrentUserStatus(member.getStatus());
                    dto.setCurrentUserRole(member.getRole());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GroupDTO> findDiscoverGroups(Integer userId) {
        return groupRepository.findDiscoverGroups(userId).stream().map(group -> {
            return mapToDTO(group, userId);
        }).collect(Collectors.toList());
    }

    @Override
    public List<GroupDTO> searchGroups(String query, Integer userId) {
        return groupRepository.searchByKeyword(query).stream()
                .map(group -> mapToDTO(group, userId))
                .collect(Collectors.toList());
    }

    @Override
    public GroupDTO findById(Integer id, Integer userId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return mapToDTO(group, userId);
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
            UserAvatar avatar = userAvatarRepository.findByUserIdAndIsCurrentTrue(member.getUser().getId());
            String avatarUrl = (avatar != null && avatar.getMedia() != null) ? avatar.getMedia().getUrl()
                    : "https://cdn-icons-png.flaticon.com/512/149/149071.png";

            UserProfile profile = userProfileRepository.findByUserId(member.getUser().getId()).orElse(null);
            String fullName = profile != null ? profile.getFullName() : member.getUser().getUsername();

            return new TungGroupMemberDTO(
                    member.getUser().getId(),
                    member.getUser().getUsername(),
                    fullName,
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
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        if (group.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Admin nhóm không thể rời nhóm, hãy chọn admin trước");
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
            groupMemberRepository.findById(inviterPk).ifPresent(inviter -> {
                if ("ADMIN".equals(inviter.getRole())) {
                    // Note: Here we consider ADMIN role or being the OWNER (if owner role is also
                    // ADMIN)
                    // If your system separates OWNER and ADMIN, you might need extra checks or use
                    // group.getOwner().getId()
                }
            });

            // Improved check for Admin/Owner
            Group inviterGroup = member.getGroup();
            if (inviterGroup.getOwner() != null && inviterGroup.getOwner().getId().equals(member.getInvitedById())) {
                invitedByAdmin = true;
            } else {
                GroupMemberId inviterPkAlt = new GroupMemberId();
                inviterPkAlt.setGroupId(groupId);
                inviterPkAlt.setUserId(member.getInvitedById());
                Optional<GroupMember> inviterOpt = groupMemberRepository.findById(inviterPkAlt);
                if (inviterOpt.isPresent() && "ADMIN".equals(inviterOpt.get().getRole())) {
                    invitedByAdmin = true;
                }
            }
        }

        boolean isPublicGroup = "PUBLIC".equals(member.getGroup().getPrivacy());

        if (invitedByAdmin || isPublicGroup) {
            member.setStatus("ACCEPTED");
            member.setJoinedAt(Instant.now());
        } else {
            // Private group invited by a regular member
            member.setStatus("REQUESTED");
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
                .filter(member -> !member.getGroup().getIsDeleted())
                .map(member -> {
                    GroupDTO dto = mapToDTO(member.getGroup());
                    dto.setCurrentUserStatus(member.getStatus());
                    dto.setCurrentUserRole(member.getRole());
                    return dto;
                })
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
            GroupMember existing = groupMemberRepository.findById(id).orElse(null);
            if (existing != null) {
                if ("ACCEPTED".equals(existing.getStatus())) {
                    throw new RuntimeException("Bạn đã là thành viên của nhóm này rồi.");
                }
                if ("REQUESTED".equals(existing.getStatus())) {
                    throw new RuntimeException("Yêu cầu tham gia của bạn đang chờ phê duyệt.");
                }
                if ("PENDING".equals(existing.getStatus())) {
                    // If user has an invitation and tries to join manually:
                    // We automatically accept them if the group is PUBLIC.
                    if ("PUBLIC".equals(group.getPrivacy())) {
                        existing.setStatus("ACCEPTED");
                        existing.setJoinedAt(Instant.now());
                        groupMemberRepository.save(existing);
                        return;
                    }
                    throw new RuntimeException(
                            "Bạn có một lời mời tham gia nhóm này. Vui lòng chấp nhận trong tab Lời mời.");
                }
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

        // Create Notification
        Notification notification = new Notification();
        notification.setUser(member.getUser());
        notification.setActor(admin.getUser());
        notification.setType("GROUP_JOIN_APPROVED");
        notification.setTargetType("GROUP");
        notification.setTargetId(groupId);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notification.setContent("Bạn đã tham gia vào nhóm " + member.getGroup().getName());
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

        groupMemberRepository.delete(member);
    }

    @Override
    public List<TungGroupMemberDTO> getPendingJoinRequests(Integer groupId, Integer adminId) {
        groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại hoặc đã bị xóa"));

        return groupMemberRepository.findAllByIdGroupIdAndStatus(groupId, "REQUESTED").stream().map(member -> {
            org.example.connectcg_be.entity.UserAvatar avatar = userAvatarRepository
                    .findByUserIdAndIsCurrentTrue(member.getUser().getId());
            String avatarUrl = avatar != null ? avatar.getMedia().getUrl()
                    : "https://cdn-icons-png.flaticon.com/512/149/149071.png";

            UserProfile profile = userProfileRepository.findByUserId(member.getUser().getId()).orElse(null);
            String fullName = profile != null ? profile.getFullName() : member.getUser().getUsername();

            return new TungGroupMemberDTO(
                    member.getUser().getId(),
                    member.getUser().getUsername(),
                    fullName,
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

        // Delete membership
        GroupMemberId targetPk = new GroupMemberId();
        targetPk.setGroupId(groupId);
        targetPk.setUserId(targetUserId);
        groupMemberRepository.deleteById(targetPk);
    }

    @Override
    @Transactional
    public void transferOwnershipAndLeave(Integer groupId, Integer newOwnerId, Integer currentOwnerId) {
        // 1. Tìm nhóm và validate Owner hiện tại
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        if (!group.getOwner().getId().equals(currentOwnerId)) {
            throw new RuntimeException("Chỉ chủ sở hữu mới có quyền chuyển nhượng");
        }

        // 2. Validate không thể chuyển cho chính mình
        if (newOwnerId.equals(currentOwnerId)) {
            throw new RuntimeException("Không thể chuyển quyền cho chính mình");
        }

        // 3. Tìm Owner mới
        User newOwner = userService.findByIdUser(newOwnerId);

        // 4. Tìm membership của Owner mới
        GroupMemberId newOwnerMemberId = new GroupMemberId();
        newOwnerMemberId.setGroupId(groupId);
        newOwnerMemberId.setUserId(newOwnerId);

        GroupMember newOwnerMember = groupMemberRepository.findById(newOwnerMemberId)
                .orElseThrow(() -> new RuntimeException("Người được chọn không phải thành viên nhóm"));

        // 5. Chuyển quyền Owner trong bảng Group
        group.setOwner(newOwner);
        groupRepository.save(group);

        // 6. Cập nhật role của Owner mới thành ADMIN (nếu chưa phải)
        newOwnerMember.setRole("ADMIN");
        groupMemberRepository.save(newOwnerMember);

        // 7. Xóa membership của Owner cũ (tự động rời nhóm)
        GroupMemberId oldOwnerMemberId = new GroupMemberId();
        oldOwnerMemberId.setGroupId(groupId);
        oldOwnerMemberId.setUserId(currentOwnerId);
        groupMemberRepository.deleteById(oldOwnerMemberId);

        // 8. Create Notification for new owner
        Notification notification = new Notification();
        notification.setUser(newOwner);
        notification.setActor(userService.findByIdUser(currentOwnerId));
        notification.setType("GROUP_OWNER_CHANGE");
        notification.setTargetType("GROUP");
        notification.setTargetId(groupId);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notification.setContent("Bạn đã được chuyển quyền sở hữu nhóm " + group.getName());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void updateMemberRole(Integer groupId, Integer targetUserId, String newRole, Integer actorId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        // 1. Phân quyền: Ai có quyền thực hiện?
        GroupMemberId actorPk = new GroupMemberId();
        actorPk.setGroupId(groupId);
        actorPk.setUserId(actorId);
        GroupMember actor = groupMemberRepository.findById(actorPk)
                .orElseThrow(() -> new RuntimeException("Bạn không phải thành viên nhóm"));

        boolean isActorOwner = group.getOwner().getId().equals(actorId);
        boolean isActorAdmin = "ADMIN".equals(actor.getRole());

        if (!isActorOwner && !isActorAdmin) {
            throw new RuntimeException("Chỉ Quản trị viên mới có quyền đổi vai trò");
        }

        // 2. Tìm thành viên mục tiêu
        GroupMemberId targetPk = new GroupMemberId();
        targetPk.setGroupId(groupId);
        targetPk.setUserId(targetUserId);
        GroupMember target = groupMemberRepository.findById(targetPk)
                .orElseThrow(() -> new RuntimeException("Người dùng không phải thành viên nhóm"));

        // 3. Xử lý các trường hợp đặc biệt
        if (targetUserId.equals(actorId)) {
            throw new RuntimeException("Bạn không thể tự đổi vai trò của chính mình");
        }

        // Nếu người bị đổi là Owner, không ai được đụng vào trừ khi chính Owner chuyển
        // quyền
        boolean isTargetOwner = group.getOwner().getId().equals(targetUserId);
        if (isTargetOwner && !"ADMIN".equals(newRole)) {
            throw new RuntimeException("Chủ nhóm bắt buộc phải có quyền Admin");
        }

        // 4. Thực hiện thay đổi (Chỉ cho phép chuyển quyền Owner)
        if (!"OWNER".equals(newRole)) {
            throw new RuntimeException("Chỉ có thể chuyển quyền chủ sở hữu, không thể thêm quản trị viên khác");
        }

        if (!isActorOwner) {
            throw new RuntimeException("Chỉ chủ nhóm hiện tại mới có quyền chuyển nhượng nhóm");
        }

        // Chuyển quyền chủ nhóm
        group.setOwner(target.getUser());
        groupRepository.save(group);

        // Người mới là Admin, người cũ trở về làm Thành viên thường
        target.setRole("ADMIN");
        actor.setRole("MEMBER");
        groupMemberRepository.save(target);
        groupMemberRepository.save(actor);

        // Thông báo cho Owner mới
        Notification n = new Notification();
        n.setUser(target.getUser());
        n.setActor(actor.getUser());
        n.setType("GROUP_OWNER_CHANGE");
        n.setTargetType("GROUP");
        n.setTargetId(groupId);
        n.setIsRead(false);
        n.setCreatedAt(Instant.now());
        n.setContent("Bạn đã được chuyển quyền sở hữu nhóm " + group.getName());
        notificationRepository.save(n);
    }
}
