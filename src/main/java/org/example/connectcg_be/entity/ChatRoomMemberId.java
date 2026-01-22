package org.example.connectcg_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ChatRoomMemberId implements Serializable {
    private static final long serialVersionUID = -7261777927805352967L;
    @NotNull
    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ChatRoomMemberId entity = (ChatRoomMemberId) o;
        return Objects.equals(this.chatRoomId, entity.chatRoomId) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatRoomId, userId);
    }

}