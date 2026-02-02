package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.ChatRoomDTO;
import org.example.connectcg_be.entity.User;

import java.util.List;

public interface ChatRoomService {
    ChatRoomDTO getOrCreateDirectChat(User user1, User user2);

    ChatRoomDTO createGroupChat(User creator, String name, List<User> members);

    List<ChatRoomDTO> getUserChatRooms(Integer userId);

    ChatRoomDTO renameRoom(Long roomId, String newName, User currentUser);

    ChatRoomDTO updateAvatar(Long roomId, String avatarUrl, User currentUser);

    ChatRoomDTO inviteMembers(Long roomId, List<Integer> invitedUserIds, User currentUser);

    ChatRoomDTO convertToDTO(org.example.connectcg_be.entity.ChatRoom room, Integer currentUserId);

    void updateLastMessageAt(String firebaseRoomKey, User sender);

    void deleteChatRoom(Long roomId, User currentUser);

    ChatRoomDTO removeMember(Long roomId, Integer userIdToRemove, User currentUser);

    void markAsRead(Long roomId, User currentUser);

    void clearHistory(Long roomId, User currentUser);

    void leaveChatRoom(Long roomId, User currentUser);
}
