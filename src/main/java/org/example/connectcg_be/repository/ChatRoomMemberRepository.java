package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.ChatRoomMember;
import org.example.connectcg_be.entity.ChatRoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, ChatRoomMemberId> {
}
