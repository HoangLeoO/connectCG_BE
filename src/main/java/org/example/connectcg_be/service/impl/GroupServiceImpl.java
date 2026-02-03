package org.example.connectcg_be.service.impl;

import jakarta.transaction.Transactional;
import org.example.connectcg_be.dto.CreateGroup;
import org.example.connectcg_be.dto.GroupDTO;
import org.example.connectcg_be.dto.TungGroupMemberDTO;
import org.example.connectcg_be.dto.TungNotificationDTO;
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
    private NotificationService notificationService;
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public org.springframework.data.domain.Page<GroupDTO> findAllGroups(
            org.springframework.data.domain.Pageable pageable) {
        return groupRepository.findAll(pageable).map(this::mapToDTO);
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
    public org.springframework.data.domain.Page<GroupDTO> findMyGroups(Integer userId,
            org.springframework.data.domain.Pageable pageable) {
        return groupRepository.findMyGroups(userId, pageable).map(g -> this.mapToDTO(g, userId));
    }

    @Override
    @Transactional
    public org.springframework.data.domain.Page<GroupDTO> findDiscoverGroups(Integer userId,
            org.springframework.data.domain.Pageable pageable) {
        return groupRepository.findDiscoverGroups(userId, pageable).map(g -> this.mapToDTO(g, userId));
    }

    @Override
    @Transactional
    public org.springframework.data.domain.Page<GroupDTO> searchGroups(String query, Integer userId,
            org.springframework.data.domain.Pageable pageable) {
        return groupRepository.searchByKeyword(query, pageable).map(g -> this.mapToDTO(g, userId));
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
        // group.setPrivacy(request.getPrivacy()); // Locked for security and AI
        // moderation reasons

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            Media media = mediaService.createCoverMedia(request.getImage(), userId);
            group.setCoverMedia(media);
        }

        return groupRepository.save(group);
    }

    @Override
    public List<TungGroupMemberDTO> getMembers(Integer groupId, Integer requesterId) {
        // Access is already checked by @PreAuthorize in GroupController
        // Logically we only return accepted members for standard view
        return groupMemberRepository.findAllByIdGroupIdAndStatus(groupId, "ACCEPTED").stream()
                .map(this::mapToMemberDTO)
                .collect(Collectors.toList());
    }

    private TungGroupMemberDTO mapToMemberDTO(GroupMember member) {
        UserAvatar avatar = userAvatarRepository.findByUserIdAndIsCurrentTrue(member.getUser().getId());
        String avatarUrl = (avatar != null && avatar.getMedia() != null) ? avatar.getMedia().getUrl()
                : "https://cdn-icons-png.flaticon.com/512/149/149071.png";

        UserProfile profile = userProfileRepository.findByUserId(member.getUser().getId()).orElse(null);
        String fullName = profile != null ? profile.getFullName() : member.getUser().getUsername();

        TungGroupMemberDTO dto = new TungGroupMemberDTO();
        dto.setUserId(member.getUser().getId());
        dto.setUsername(member.getUser().getUsername());
        dto.setFullName(fullName);
        dto.setAvatarUrl(avatarUrl);
        dto.setRole(member.getRole());
        dto.setStatus(member.getStatus());
        dto.setJoinedAt(member.getJoinedAt());
        dto.setViolationCount(member.getViolationCount());
        dto.setLastViolationAt(member.getLastViolationAt());
        return dto;
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

        User userLeaving = userService.findByIdUser(userId);
        groupMemberRepository.deleteById(memberId);

        // Notify Owner/Admins that a member left
        String actorFullName = userProfileRepository.findByUserId(userId)
                .map(UserProfile::getFullName)
                .orElse(userLeaving.getUsername());

        TungNotificationDTO noti = new TungNotificationDTO();
        noti.setContent(actorFullName + " đã rời khỏi nhóm " + group.getName());
        noti.setType("GROUP_MEMBER_LEFT");
        noti.setTargetType("GROUP");
        noti.setTargetId(groupId);

        notificationService.sendNotification(noti, group.getOwner(), userLeaving);

        // Broadcast realtime
        org.example.connectcg_be.dto.MembershipEventDTO event = new org.example.connectcg_be.dto.MembershipEventDTO(
                "LEFT", groupId, userId, null);
        messagingTemplate.convertAndSend("/topic/groups/membership", event);
    }

    @Override
    @Transactional
    public void deleteGroup(Integer groupId, Integer userId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User requester = userService.findByIdUser(userId);
        boolean isOwner = group.getOwner() != null && group.getOwner().getId().equals(userId);
        boolean isSystemAdmin = requester != null && "ADMIN".equals(requester.getRole());
        if (!isOwner && !isSystemAdmin) {
            throw new RuntimeException("Chỉ chủ nhóm hoặc admin hệ thống mới có quyền xóa nhóm");
        }

        group.setIsDeleted(true);
        groupRepository.save(group);

        // Fetch all current members to notify
        List<GroupMember> members = groupMemberRepository.findAllByIdGroupIdAndStatus(groupId, "ACCEPTED");
        for (GroupMember m : members) {
            TungNotificationDTO noti = new TungNotificationDTO();
            noti.setType("GROUP_DELETED");
            noti.setTargetType("GROUP");
            noti.setTargetId(groupId);

            if (m.getUser().getId().equals(group.getOwner().getId())) {
                noti.setContent("Nhóm '" + group.getName() + "' của bạn đã bị xóa.");
            } else {
                noti.setContent("Nhóm '" + group.getName() + "' đã bị xóa bởi quản trị viên.");
            }
            notificationService.sendNotification(noti, m.getUser(), requester);
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
            User user = userService.findByIdUser(userId);

            GroupMemberId id = new GroupMemberId(groupId, userId);
            Optional<GroupMember> existing = groupMemberRepository.findById(id);

            if (existing.isPresent()) {
                if ("BANNED".equals(existing.get().getStatus())) {
                    continue; // Skip banned users
                }
                if ("ACCEPTED".equals(existing.get().getStatus())) {
                    continue; // Already a member
                }
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

            // Notify the invited user
            User actor = userService.findByIdUser(actorId);
            String actorFullName = userProfileRepository.findByUserId(actorId)
                    .map(UserProfile::getFullName)
                    .orElse(actor.getUsername());

            TungNotificationDTO dto = new TungNotificationDTO();
            dto.setContent(actorFullName + " đã mời bạn tham gia nhóm " + group.getName());
            dto.setType("GROUP_INVITE");
            dto.setTargetType("GROUP");
            dto.setTargetId(groupId);
            notificationService.sendNotification(dto, user, actor);
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

        if ("BANNED".equals(member.getStatus())) {
            throw new RuntimeException("Bạn đã bị cấm khỏi nhóm này.");
        }

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

        // Notify the inviter that their invitation was accepted
        if (member.getInvitedById() != null && "ACCEPTED".equals(member.getStatus())) {
            User inviter = userService.findByIdUser(member.getInvitedById());
            User actor = member.getUser();
            String actorFullName = userProfileRepository.findByUserId(actor.getId())
                    .map(UserProfile::getFullName)
                    .orElse(actor.getUsername());

            TungNotificationDTO dto = new TungNotificationDTO();
            dto.setContent(actorFullName + " đã chấp nhận lời mời tham gia nhóm " + member.getGroup().getName());
            dto.setType("GROUP_INVITE_ACCEPTED");
            dto.setTargetType("GROUP");
            dto.setTargetId(groupId);
            notificationService.sendNotification(dto, inviter, actor);
        }
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

        Optional<GroupMember> existingMember = groupMemberRepository.findById(id);
        if (existingMember.isPresent()) {
            GroupMember member = existingMember.get();
            if ("BANNED".equals(member.getStatus())) {
                throw new RuntimeException("Bạn đã bị cấm khỏi nhóm này do vi phạm quy định nhiều lần.");
            }
            if ("ACCEPTED".equals(member.getStatus())) {
                throw new RuntimeException("Bạn đã là thành viên của nhóm này rồi.");
            }

            if ("REQUESTED".equals(member.getStatus())) {
                throw new RuntimeException("Yêu cầu tham gia của bạn đang chờ phê duyệt.");
            }

            if ("PENDING".equals(member.getStatus())) {
                // If user has an invitation and tries to join manually:
                if ("PUBLIC".equals(group.getPrivacy())) {
                    member.setStatus("ACCEPTED");
                    member.setJoinedAt(Instant.now());
                    groupMemberRepository.save(member);

                    // Notify the group owner
                    User owner = group.getOwner();
                    if (owner != null && !owner.getId().equals(userId)) {
                        String actorFullName = userProfileRepository.findByUserId(userId)
                                .map(UserProfile::getFullName)
                                .orElse(user.getUsername());
                        TungNotificationDTO dto = new TungNotificationDTO();
                        dto.setContent(actorFullName + " đã tham gia vào nhóm " + group.getName());
                        dto.setType("GROUP_MEMBER_JOINED");
                        dto.setTargetType("GROUP");
                        dto.setTargetId(groupId);
                        notificationService.sendNotification(dto, owner, user);
                    }

                    // Broadcast realtime
                    org.example.connectcg_be.dto.MembershipEventDTO event = new org.example.connectcg_be.dto.MembershipEventDTO(
                            "JOINED", groupId, userId, mapToMemberDTO(member));
                    messagingTemplate.convertAndSend("/topic/groups/membership", event);

                    return;
                }
                throw new RuntimeException(
                        "Bạn đang có một lời mời gia nhập nhóm này. Vui lòng chấp nhận lời mời để tiếp tục.");
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

        User owner = group.getOwner();
        if (owner != null && !owner.getId().equals(userId)) {
            String actorFullName = userProfileRepository.findByUserId(userId)
                    .map(UserProfile::getFullName)
                    .orElse(user.getUsername());

            String type = "PRIVATE".equals(group.getPrivacy()) ? "GROUP_JOIN_REQUEST" : "GROUP_MEMBER_JOINED";
            String content = "PRIVATE".equals(group.getPrivacy())
                    ? actorFullName + " đã yêu cầu tham gia nhóm " + group.getName()
                    : actorFullName + " đã tham gia vào nhóm " + group.getName();

            TungNotificationDTO dto = new TungNotificationDTO();
            dto.setContent(content);
            dto.setType(type);
            dto.setTargetType("GROUP");
            dto.setTargetId(groupId);
            notificationService.sendNotification(dto, owner, user);
        }

        // Broadcast realtime
        org.example.connectcg_be.dto.MembershipEventDTO event = new org.example.connectcg_be.dto.MembershipEventDTO(
                "ACCEPTED".equals(member.getStatus()) ? "JOINED" : "REQUESTED",
                groupId, userId, mapToMemberDTO(member));
        messagingTemplate.convertAndSend("/topic/groups/membership", event);
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

        TungNotificationDTO dto = new TungNotificationDTO();
        dto.setContent("Bạn đã tham gia vào nhóm " + member.getGroup().getName());
        dto.setType("GROUP_JOIN_APPROVED");
        dto.setTargetType("GROUP");
        dto.setTargetId(groupId);
        notificationService.sendNotification(dto, member.getUser(), admin.getUser());

        // Broadcast realtime
        org.example.connectcg_be.dto.MembershipEventDTO event = new org.example.connectcg_be.dto.MembershipEventDTO(
                "APPROVED", groupId, targetUserId, mapToMemberDTO(member));
        messagingTemplate.convertAndSend("/topic/groups/membership", event);
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

        User admin = userService.findByIdUser(adminId);
        TungNotificationDTO dto = new TungNotificationDTO();
        dto.setContent("Yêu cầu gia nhập nhóm " + member.getGroup().getName() + " của bạn đã bị từ chối");
        dto.setType("GROUP_JOIN_REJECTED");
        dto.setTargetType("GROUP");
        dto.setTargetId(groupId);
        notificationService.sendNotification(dto, member.getUser(), admin);

        // Broadcast realtime
        org.example.connectcg_be.dto.MembershipEventDTO event = new org.example.connectcg_be.dto.MembershipEventDTO(
                "REJECTED", groupId, targetUserId, null);
        messagingTemplate.convertAndSend("/topic/groups/membership", event);
    }

    @Override
    public List<TungGroupMemberDTO> getPendingJoinRequests(Integer groupId, Integer adminId) {
        groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm của bạn không tồn tại"));

        return groupMemberRepository.findAllByIdGroupIdAndStatus(groupId, "REQUESTED").stream()
                .map(this::mapToMemberDTO)
                .collect(Collectors.toList());
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
        GroupMember targetMember = groupMemberRepository.findById(targetPk).orElse(null);
        if (targetMember != null) {
            User targetUser = targetMember.getUser();
            targetMember.setStatus("BANNED");
            groupMemberRepository.save(targetMember);

            TungNotificationDTO dto = new TungNotificationDTO();
            dto.setContent("Bạn đã bị cấm khỏi nhóm " + group.getName());
            dto.setType("GROUP_BANNED");
            dto.setTargetType("GROUP");
            dto.setTargetId(groupId);
            notificationService.sendNotification(dto, targetUser, requester.getUser());

            // Broadcast realtime
            org.example.connectcg_be.dto.MembershipEventDTO event = new org.example.connectcg_be.dto.MembershipEventDTO(
                    "BANNED", groupId, targetUserId, null);
            messagingTemplate.convertAndSend("/topic/groups/membership", event);
        }
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

        TungNotificationDTO dto = new TungNotificationDTO();
        dto.setContent("Bạn đã được ủy quyền thành admin của nhóm " + group.getName());
        dto.setType("GROUP_OWNER_CHANGE");
        dto.setTargetType("GROUP");
        dto.setTargetId(groupId);
        notificationService.sendNotification(dto, newOwner, userService.findByIdUser(currentOwnerId));
    }

    @Override
    @Transactional
    public void updateMemberRole(Integer groupId, Integer targetUserId, String newRole, Integer actorId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        GroupMemberId actorPk = new GroupMemberId(groupId, actorId);
        GroupMember actor = groupMemberRepository.findById(actorPk)
                .orElseThrow(() -> new RuntimeException("Lỗi permission"));

        if (!"ADMIN".equals(actor.getRole()) && !group.getOwner().getId().equals(actorId)) {
            throw new RuntimeException("Chỉ admin mới có quyền đổi vai trò");
        }

        GroupMemberId targetPk = new GroupMemberId(groupId, targetUserId);
        GroupMember target = groupMemberRepository.findById(targetPk)
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại"));

        // Don't allow changing owner's role through this method directly if it's not a
        // transfer
        if (group.getOwner().getId().equals(targetUserId)) {
            throw new RuntimeException("Không thể thay đổi vai trò của chủ nhóm tại đây");
        }

        target.setRole(newRole);
        groupMemberRepository.save(target);

        TungNotificationDTO dto = new TungNotificationDTO();
        String roleName = "ADMIN".equals(newRole) ? "Quản trị viên" : "Thành viên";
        dto.setContent("Vai trò của bạn trong nhóm " + group.getName() + " đã được thay đổi thành " + roleName);
        dto.setType("GROUP_ROLE_CHANGED");
        dto.setTargetType("GROUP");
        dto.setTargetId(groupId);

        notificationService.sendNotification(dto, target.getUser(), actor.getUser());
    }

    @Override
    public List<TungGroupMemberDTO> getBannedMembers(Integer groupId, Integer requesterId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        // Check if requester is Admin or Owner
        GroupMemberId requesterPk = new GroupMemberId();
        requesterPk.setGroupId(groupId);
        requesterPk.setUserId(requesterId);
        GroupMember requester = groupMemberRepository.findById(requesterPk).orElse(null);

        boolean isAdmin = requester != null
                && ("ADMIN".equals(requester.getRole()) || "OWNER".equals(requester.getRole()));
        boolean isOwner = group.getOwner() != null && group.getOwner().getId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Bạn không có quyền xem danh sách thành viên bị cấm");
        }

        return groupMemberRepository.findAllByIdGroupIdAndStatus(groupId, "BANNED").stream()
                .map(this::mapToMemberDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void unbanMember(Integer groupId, Integer targetUserId, Integer adminId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        // Check if requester is Admin or Owner
        GroupMemberId adminPk = new GroupMemberId();
        adminPk.setGroupId(groupId);
        adminPk.setUserId(adminId);
        GroupMember admin = groupMemberRepository.findById(adminPk)
                .orElseThrow(() -> new RuntimeException("Bạn không phải là thành viên của nhóm"));

        boolean isAdminOrOwner = "ADMIN".equals(admin.getRole()) || "OWNER".equals(admin.getRole());
        boolean isOwner = group.getOwner() != null && group.getOwner().getId().equals(adminId);

        if (!isAdminOrOwner && !isOwner) {
            throw new RuntimeException("Bạn không có quyền gỡ lệnh cấm");
        }

        // Find banned member
        GroupMemberId targetPk = new GroupMemberId();
        targetPk.setGroupId(groupId);
        targetPk.setUserId(targetUserId);
        GroupMember target = groupMemberRepository.findById(targetPk)
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại trong nhóm"));

        if (!"BANNED".equals(target.getStatus())) {
            throw new RuntimeException("Thành viên này không bị cấm");
        }

        // Unban: Reset status and violation count
        target.setStatus("ACCEPTED");
        target.setViolationCount(0);
        target.setLastViolationAt(null);
        groupMemberRepository.save(target);

        // Send notification
        TungNotificationDTO dto = new TungNotificationDTO();
        dto.setContent("Bạn đã được gỡ lệnh cấm khỏi nhóm " + group.getName());
        dto.setType("GROUP_UNBAN");
        dto.setTargetType("GROUP");
        dto.setTargetId(groupId);
        notificationService.sendNotification(dto, target.getUser(), admin.getUser());
    }
}
