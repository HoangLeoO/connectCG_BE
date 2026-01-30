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
import org.example.connectcg_be.service.NotificationService;
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
    @Autowired
    private NotificationService notificationService;

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

        User requesterUser = userService.findByIdUser(requesterId);
        boolean isSystemAdmin = "ADMIN".equals(requesterUser.getRole());

        if ("PRIVATE".equals(group.getPrivacy()) && !isSystemAdmin) {
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
            throw new RuntimeException("Admin nhóm không thể rời nhóm, hãy chọn một admin khác trước khi rời");
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

        User requester = userService.findByIdUser(userId);
        // Cho phép xóa nếu là Owner hoặc Admin hệ thống
        boolean isOwner = group.getOwner() != null && group.getOwner().getId().equals(userId);
        boolean isSystemAdmin = requester != null && "ADMIN".equals(requester.getRole());
        if (!isOwner && !isSystemAdmin) {
            throw new RuntimeException("Chỉ chủ nhóm hoặc admin hệ thống mới có quyền xóa nhóm");
        }

        group.setIsDeleted(true);
        groupRepository.save(group);
        User owner = group.getOwner();
        if (owner != null) {
            org.example.connectcg_be.dto.TungNotificationDTO noti = new org.example.connectcg_be.dto.TungNotificationDTO();
            noti.setContent("Nhóm '" + group.getName() + "' của bạn đã bị xóa do vi phạm quy tắc cộng đồng.");
            noti.setType("GROUP_DELETED");
            noti.setTargetType("GROUP");
            noti.setTargetId(groupId);

            // Gọi hàm vừa viết để lưu DB + Gửi WebSocket
            notificationService.sendNotification(noti, owner);
        }
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
            throw new RuntimeException("Lỗi");
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời tham gia"));

        if (!"PENDING".equals(member.getStatus())) {
            throw new RuntimeException("Lời mời không ở trạng thái PENDING");
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
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

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
                            "Lỗi");
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin trong nhóm"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Chỉ admin mới có quyền phê duyệt yêu cầu");
        }

        GroupMemberId targetPk = new GroupMemberId();
        targetPk.setGroupId(groupId);
        targetPk.setUserId(targetUserId);
        GroupMember member = groupMemberRepository.findById(targetPk)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu tham gia nhóm"));

        if (!"REQUESTED".equals(member.getStatus())) {
            throw new RuntimeException("Trạng thái thành viên không phải là REQUESTED");
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu tham gia nhóm"));

        if (!"REQUESTED".equals(member.getStatus())) {
            throw new RuntimeException("Trạng thái thành viên không phải là REQUESTED");
        }


        groupMemberRepository.delete(member);
    }

    @Override
    public List<TungGroupMemberDTO> getPendingJoinRequests(Integer groupId, Integer adminId) {
        groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm của bạn không tồn tại"));

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
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        // 1. Check if requester is Admin or Owner
        GroupMemberId adminPk = new GroupMemberId();
        adminPk.setGroupId(groupId);
        adminPk.setUserId(adminId);
        GroupMember requester = groupMemberRepository.findById(adminPk).orElse(null);

        boolean isRequesterAdmin = requester != null && "ADMIN".equals(requester.getRole());
        boolean isRequesterOwner = group.getOwner().getId().equals(adminId);

        if (!isRequesterAdmin && !isRequesterOwner) {
            throw new RuntimeException("Chỉ Admin mới có quyền loại thành viên");
        }

// 2. Không thể kick chính mình
        if (targetUserId.equals(adminId)) {
            throw new RuntimeException("Bạn không thể kick chính mình");
        }

// 3. Không thể kick Owner
        if (group.getOwner().getId().equals(targetUserId)) {
            throw new RuntimeException("Không thể kick chủ nhóm");
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

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tại"));

        if (!group.getOwner().getId().equals(currentOwnerId)) {
            throw new RuntimeException("Lỗi");
        }


        if (newOwnerId.equals(currentOwnerId)) {
            throw new RuntimeException(" Lỗi");
        }


        User newOwner = userService.findByIdUser(newOwnerId);

        GroupMemberId newOwnerMemberId = new GroupMemberId();
        newOwnerMemberId.setGroupId(groupId);
        newOwnerMemberId.setUserId(newOwnerId);

        GroupMember newOwnerMember = groupMemberRepository.findById(newOwnerMemberId)
                .orElseThrow(() -> new RuntimeException("Người dùng không phải thành viên trong nhóm"));


        group.setOwner(newOwner);
        groupRepository.save(group);


        newOwnerMember.setRole("ADMIN");
        groupMemberRepository.save(newOwnerMember);


        GroupMemberId oldOwnerMemberId = new GroupMemberId();
        oldOwnerMemberId.setGroupId(groupId);
        oldOwnerMemberId.setUserId(currentOwnerId);
        groupMemberRepository.deleteById(oldOwnerMemberId);


        Notification notification = new Notification();
        notification.setUser(newOwner);
        notification.setActor(userService.findByIdUser(currentOwnerId));
        notification.setType("GROUP_OWNER_CHANGE");
        notification.setTargetType("GROUP");
        notification.setTargetId(groupId);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notification.setContent("Bạn đã được ủy quyền thành ad của nhóm " + group.getName());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void updateMemberRole(Integer groupId, Integer targetUserId, String newRole, Integer actorId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));


        GroupMemberId actorPk = new GroupMemberId();
        actorPk.setGroupId(groupId);
        actorPk.setUserId(actorId);
        GroupMember actor = groupMemberRepository.findById(actorPk)
                .orElseThrow(() -> new RuntimeException("Lỗi"));

        boolean isActorOwner = group.getOwner().getId().equals(actorId);
        boolean isActorAdmin = "ADMIN".equals(actor.getRole());

        if (!isActorOwner && !isActorAdmin) {
            throw new RuntimeException("Lỗi");
        }


        GroupMemberId targetPk = new GroupMemberId();
        targetPk.setGroupId(groupId);
        targetPk.setUserId(targetUserId);
        GroupMember target = groupMemberRepository.findById(targetPk)
                .orElseThrow(() -> new RuntimeException("Lỗi"));


        if (targetUserId.equals(actorId)) {
            throw new RuntimeException("Lỗi");
        }


        boolean isTargetOwner = group.getOwner().getId().equals(targetUserId);
        if (isTargetOwner && !"ADMIN".equals(newRole)) {
            throw new RuntimeException("Lỗi");
        }


        if (!"OWNER".equals(newRole)) {
            throw new RuntimeException("Lỗi");
        }

        if (!isActorOwner) {
            throw new RuntimeException("Lỗi");
        }
        group.setOwner(target.getUser());
        groupRepository.save(group);


        target.setRole("ADMIN");
        actor.setRole("MEMBER");
        groupMemberRepository.save(target);
        groupMemberRepository.save(actor);

        Notification n = new Notification();
        n.setUser(target.getUser());
        n.setActor(actor.getUser());
        n.setType("GROUP_OWNER_CHANGE");
        n.setTargetType("GROUP");
        n.setTargetId(groupId);
        n.setIsRead(false);
        n.setCreatedAt(Instant.now());
        n.setContent("Bạn đã được ủy quyền thành admin nhóm " + group.getName());
        notificationRepository.save(n);
    }
}

