package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.ChatMemberDTO;
import org.example.connectcg_be.dto.ChatRoomDTO;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserAvatarRepository userAvatarRepository;

    @Override
    @Transactional
    public ChatRoomDTO getOrCreateDirectChat(User user1, User user2) {
        // Tìm phòng chat chung giữa 2 người
        List<ChatRoomMember> memberships1 = chatRoomMemberRepository.findByUser_Id(user1.getId());
        for (ChatRoomMember m1 : memberships1) {
            ChatRoom room = m1.getChatRoom();
            if ("DIRECT".equals(room.getType())) {
                Optional<ChatRoomMember> m2 = chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(room.getId(),
                        user2.getId());
                if (m2.isPresent()) {
                    return convertToDTO(room, user1.getId());
                }
            }
        }

        // Nếu chưa có, tạo mới
        ChatRoom room = new ChatRoom();
        room.setType("DIRECT");
        room.setFirebaseRoomKey(UUID.randomUUID().toString());
        room.setCreatedBy(user1);
        room.setCreatedAt(Instant.now());
        room.setIsActive(true);
        room = chatRoomRepository.save(room);

        // Add members
        addMember(room, user1, "ADMIN");
        addMember(room, user2, "MEMBER");

        return convertToDTO(room, user1.getId());
    }

    @Override
    @Transactional
    public ChatRoomDTO createGroupChat(User creator, String name, List<User> members) {
        ChatRoom room = new ChatRoom();
        room.setType("GROUP");
        room.setName(name);
        room.setFirebaseRoomKey(UUID.randomUUID().toString());
        room.setCreatedBy(creator);
        room.setCreatedAt(Instant.now());
        room.setIsActive(true);
        room = chatRoomRepository.save(room);

        addMember(room, creator, "ADMIN");
        for (User member : members) {
            if (!member.getId().equals(creator.getId())) {
                addMember(room, member, "MEMBER");
            }
        }

        return convertToDTO(room, creator.getId());
    }

    @Override
    public List<ChatRoomDTO> getUserChatRooms(Integer userId) {
        List<ChatRoom> rooms = chatRoomMemberRepository.findByUser_IdOrderByLastMessageAtDesc(userId).stream()
                .map(ChatRoomMember::getChatRoom)
                .collect(Collectors.toList());

        return rooms.stream()
                .map(room -> convertToDTO(room, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateLastMessageAt(String firebaseRoomKey) {
        chatRoomRepository.findByFirebaseRoomKey(firebaseRoomKey).ifPresent(room -> {
            room.setLastMessageAt(Instant.now());
            chatRoomRepository.save(room);
        });
    }

    @Override
    @Transactional
    public ChatRoomDTO renameRoom(Long roomId, String newName, User currentUser) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        ChatRoomMember membership = chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this room"));

        if (!"ADMIN".equals(membership.getRole()) && !"GROUP".equals(room.getType())) {
            throw new RuntimeException("Only admins can rename group chats");
        }

        room.setName(newName);
        room = chatRoomRepository.save(room);
        return convertToDTO(room, currentUser.getId());
    }

    @Override
    @Transactional
    public ChatRoomDTO updateAvatar(Long roomId, String avatarUrl, User currentUser) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        ChatRoomMember membership = chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this room"));

        if (!"ADMIN".equals(membership.getRole()) && !"GROUP".equals(room.getType())) {
            throw new RuntimeException("Only admins can change group avatar");
        }

        room.setAvatarUrl(avatarUrl);
        room = chatRoomRepository.save(room);
        return convertToDTO(room, currentUser.getId());
    }

    @Override
    @Transactional
    public ChatRoomDTO inviteMembers(Long roomId, List<Integer> invitedUserIds, User currentUser) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!"GROUP".equals(room.getType())) {
            throw new RuntimeException("Cannot invite users to direct chat");
        }

        // Check current user is member
        chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this room"));

        // Get current member IDs to check duplicates
        List<Integer> currentMemberIds = chatRoomMemberRepository.findByChatRoom_Id(roomId)
                .stream()
                .map(m -> m.getUser().getId())
                .collect(Collectors.toList());

        // Process each invited user
        for (Integer invitedUserId : invitedUserIds) {
            // Skip if already a member
            if (currentMemberIds.contains(invitedUserId)) {
                continue;
            }

            User invitedUser = userRepository.findById(invitedUserId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + invitedUserId));

            // Add member
            addMember(room, invitedUser, "MEMBER");
        }

        return convertToDTO(room, currentUser.getId());
    }

    @Override
    public ChatRoomDTO convertToDTO(ChatRoom room, Integer currentUserId) {
        String name = room.getName();
        String avatarUrl = room.getAvatarUrl();
        Integer otherParticipantId = null;

        // Fetch all members to populate 'members' list and determine 1-1 info
        List<ChatRoomMember> roomMembers = chatRoomMemberRepository.findByChatRoom_Id(room.getId());

        List<ChatMemberDTO> memberDTOs = roomMembers.stream().map(rm -> {
            Integer uid = rm.getUser().getId();
            String uName = rm.getUser().getUsername();
            String fName = userProfileRepository.findByUserId(uid)
                    .map(UserProfile::getFullName)
                    .orElse(uName);

            String aUrl = null;
            UserAvatar avatar = userAvatarRepository.findByUserIdAndIsCurrentTrue(uid);
            if (avatar != null && avatar.getMedia() != null) {
                aUrl = avatar.getMedia().getUrl();
            }

            return ChatMemberDTO.builder()
                    .id(uid)
                    .fullName(fName)
                    .avatarUrl(aUrl)
                    .role(rm.getRole())
                    .build();
        }).collect(Collectors.toList());

        if ("DIRECT".equals(room.getType())) {
            // Find the other member specifically for the room header info
            for (ChatMemberDTO m : memberDTOs) {
                if (!m.getId().equals(currentUserId)) {
                    otherParticipantId = m.getId();
                    name = m.getFullName();
                    avatarUrl = m.getAvatarUrl();
                    break;
                }
            }
        }

        return ChatRoomDTO.builder()
                .id(room.getId())
                .type(room.getType())
                .name(name)
                .avatarUrl(avatarUrl)
                .firebaseRoomKey(room.getFirebaseRoomKey())
                .otherParticipantId(otherParticipantId)
                .members(memberDTOs)
                .lastMessageAt(room.getLastMessageAt())
                .createdAt(room.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteChatRoom(Long roomId, User currentUser) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        ChatRoomMember membership = chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this room"));

        if ("GROUP".equals(room.getType())) {
            // Chỉ ADMIN mới được xóa nhóm
            if (!"ADMIN".equals(membership.getRole())) {
                throw new RuntimeException("Only group admins can delete the room");
            }
        } else {
            // Với DIRECT chat, bất kỳ thành viên nào cũng có quyền xóa (xóa chung cho cả 2)
            // Membership check đã ở trên rồi
        }

        // 1. Xóa tất cả thành viên
        chatRoomMemberRepository.deleteByChatRoom_Id(roomId);

        // 2. Xóa phòng chat
        chatRoomRepository.delete(room);
    }

    private void addMember(ChatRoom room, User user, String role) {
        ChatRoomMember member = new ChatRoomMember();
        ChatRoomMemberId id = new ChatRoomMemberId();
        id.setChatRoomId(room.getId());
        id.setUserId(user.getId());
        member.setId(id);
        member.setChatRoom(room);
        member.setUser(user);
        member.setRole(role);
        member.setJoinedAt(Instant.now());
        chatRoomMemberRepository.save(member);
    }
}
