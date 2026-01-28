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
                .orElseThrow(() -> new RuntimeException("NhÃ³m khÃ´ng tá»“n táº¡i"));

        if (group.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Admin nhÃ³m khÃ´ng thá»ƒ rá»i nhÃ³m, hÃ£y chá»n admin trÆ°á»›c");
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
            throw new RuntimeException("Chá»‰ thÃ nh viÃªn trong nhÃ³m má»›i cÃ³ quyá»n má»i ngÆ°á»i khÃ¡c.");
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
                    throw new RuntimeException("Báº¡n Ä‘Ã£ lÃ  thÃ nh viÃªn cá»§a nhÃ³m nÃ y rá»“i.");
                }
                if ("REQUESTED".equals(existing.getStatus())) {
                    throw new RuntimeException("YÃªu cáº§u tham gia cá»§a báº¡n Ä‘ang chá» phÃª duyá»‡t.");
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
                            "Báº¡n cÃ³ má»™t lá»i má»i tham gia nhÃ³m nÃ y. Vui lÃ²ng cháº¥p nháº­n trong tab Lá»i má»i.");
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
        notification.setContent("Báº¡n Ä‘Ã£ tham gia vÃ o nhÃ³m " + member.getGroup().getName());
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
                .orElseThrow(() -> new RuntimeException("NhÃ³m khÃ´ng tá»“n táº¡i hoáº·c Ä‘Ã£ bá»‹ xÃ³a"));

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
        // 1. TÃ¬m nhÃ³m vÃ  validate Owner hiá»‡n táº¡i
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("NhÃ³m khÃ´ng tá»“n táº¡i"));

        if (!group.getOwner().getId().equals(currentOwnerId)) {
            throw new RuntimeException("Chá»‰ chá»§ sá»Ÿ há»¯u má»›i cÃ³ quyá»n chuyá»ƒn nhÆ°á»£ng");
        }

        // 2. Validate khÃ´ng thá»ƒ chuyá»ƒn cho chÃ­nh mÃ¬nh
        if (newOwnerId.equals(currentOwnerId)) {
            throw new RuntimeException("KhÃ´ng thá»ƒ chuyá»ƒn quyá»n cho chÃ­nh mÃ¬nh");
        }

        // 3. TÃ¬m Owner má»›i
        User newOwner = userService.findByIdUser(newOwnerId);

        // 4. TÃ¬m membership cá»§a Owner má»›i
        GroupMemberId newOwnerMemberId = new GroupMemberId();
        newOwnerMemberId.setGroupId(groupId);
        newOwnerMemberId.setUserId(newOwnerId);

        GroupMember newOwnerMember = groupMemberRepository.findById(newOwnerMemberId)
                .orElseThrow(() -> new RuntimeException("NgÆ°á»i Ä‘Æ°á»£c chá»n khÃ´ng pháº£i thÃ nh viÃªn nhÃ³m"));

        // 5. Chuyá»ƒn quyá»n Owner trong báº£ng Group
        group.setOwner(newOwner);
        groupRepository.save(group);

        // 6. Cáº­p nháº­t role cá»§a Owner má»›i thÃ nh ADMIN (náº¿u chÆ°a pháº£i)
        newOwnerMember.setRole("ADMIN");
        groupMemberRepository.save(newOwnerMember);

        // 7. XÃ³a membership cá»§a Owner cÅ© (tá»± Ä‘á»™ng rá»i nhÃ³m)
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
        notification.setContent("Báº¡n Ä‘Ã£ Ä‘Æ°á»£c chuyá»ƒn quyá»n sá»Ÿ há»¯u nhÃ³m " + group.getName());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void updateMemberRole(Integer groupId, Integer targetUserId, String newRole, Integer actorId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("NhÃ³m khÃ´ng tá»“n táº¡i"));

        // 1. PhÃ¢n quyá»n: Ai cÃ³ quyá»n thá»±c hiá»‡n?
        GroupMemberId actorPk = new GroupMemberId();
        actorPk.setGroupId(groupId);
        actorPk.setUserId(actorId);
        GroupMember actor = groupMemberRepository.findById(actorPk)
                .orElseThrow(() -> new RuntimeException("Báº¡n khÃ´ng pháº£i thÃ nh viÃªn nhÃ³m"));

        boolean isActorOwner = group.getOwner().getId().equals(actorId);
        boolean isActorAdmin = "ADMIN".equals(actor.getRole());

        if (!isActorOwner && !isActorAdmin) {
            throw new RuntimeException("Chá»‰ Quáº£n trá»‹ viÃªn má»›i cÃ³ quyá»n Ä‘á»•i vai trÃ²");
        }

        // 2. TÃ¬m thÃ nh viÃªn má»¥c tiÃªu
        GroupMemberId targetPk = new GroupMemberId();
        targetPk.setGroupId(groupId);
        targetPk.setUserId(targetUserId);
        GroupMember target = groupMemberRepository.findById(targetPk)
                .orElseThrow(() -> new RuntimeException("NgÆ°á»i dÃ¹ng khÃ´ng pháº£i thÃ nh viÃªn nhÃ³m"));

        // 3. Xá»­ lÃ½ cÃ¡c trÆ°á»ng há»£p Ä‘áº·c biá»‡t
        if (targetUserId.equals(actorId)) {
            throw new RuntimeException("Báº¡n khÃ´ng thá»ƒ tá»± Ä‘á»•i vai trÃ² cá»§a chÃ­nh mÃ¬nh");
        }

        // Náº¿u ngÆ°á»i bá»‹ Ä‘á»•i lÃ  Owner, khÃ´ng ai Ä‘Æ°á»£c Ä‘á»¥ng vÃ o trá»« khi chÃ­nh Owner chuyá»ƒn
        // quyá»n
        boolean isTargetOwner = group.getOwner().getId().equals(targetUserId);
        if (isTargetOwner && !"ADMIN".equals(newRole)) {
            throw new RuntimeException("Chá»§ nhÃ³m báº¯t buá»™c pháº£i cÃ³ quyá»n Admin");
        }

        // 4. Thá»±c hiá»‡n thay Ä‘á»•i (Chá»‰ cho phÃ©p chuyá»ƒn quyá»n Owner)
        if (!"OWNER".equals(newRole)) {
            throw new RuntimeException("Chá»‰ cÃ³ thá»ƒ chuyá»ƒn quyá»n chá»§ sá»Ÿ há»¯u, khÃ´ng thá»ƒ thÃªm quáº£n trá»‹ viÃªn khÃ¡c");
        }

        if (!isActorOwner) {
            throw new RuntimeException("Chá»‰ chá»§ nhÃ³m hiá»‡n táº¡i má»›i cÃ³ quyá»n chuyá»ƒn nhÆ°á»£ng nhÃ³m");
        }

        // Chuyá»ƒn quyá»n chá»§ nhÃ³m
        group.setOwner(target.getUser());
        groupRepository.save(group);

        // NgÆ°á»i má»›i lÃ  Admin, ngÆ°á»i cÅ© trá»Ÿ vá» lÃ m ThÃ nh viÃªn thÆ°á»ng
        target.setRole("ADMIN");
        actor.setRole("MEMBER");
        groupMemberRepository.save(target);
        groupMemberRepository.save(actor);

        // ThÃ´ng bÃ¡o cho Owner má»›i
        Notification n = new Notification();
        n.setUser(target.getUser());
        n.setActor(actor.getUser());
        n.setType("GROUP_OWNER_CHANGE");
        n.setTargetType("GROUP");
        n.setTargetId(groupId);
        n.setIsRead(false);
        n.setCreatedAt(Instant.now());
        n.setContent("Báº¡n Ä‘Ã£ Ä‘Æ°á»£c chuyá»ƒn quyá»n sá»Ÿ há»¯u nhÃ³m " + group.getName());
        notificationRepository.save(n);
    }
}

