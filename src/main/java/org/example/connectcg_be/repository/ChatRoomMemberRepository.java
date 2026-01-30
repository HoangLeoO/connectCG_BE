package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.ChatRoomMember;
import org.example.connectcg_be.entity.ChatRoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, ChatRoomMemberId> {
    List<ChatRoomMember> findByUser_Id(Integer userId);

    Optional<ChatRoomMember> findByChatRoom_IdAndUser_Id(Long chatRoomId, Integer userId);

    List<ChatRoomMember> findByChatRoom_Id(Long chatRoomId);

    void deleteByChatRoom_Id(Long chatRoomId);
}
