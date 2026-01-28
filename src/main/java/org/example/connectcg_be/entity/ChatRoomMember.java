package org.example.connectcg_be.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "chat_room_members")
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMember {
    @EmbeddedId
    private ChatRoomMemberId id;

    @MapsId("chatRoomId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 20)
    @ColumnDefault("'MEMBER'")
    @Column(name = "role", length = 20)
    private String role;

    @Size(max = 100)
    @Column(name = "nickname", length = 100)
    private String nickname;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

}
