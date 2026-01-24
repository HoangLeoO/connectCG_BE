package org.example.connectcg_be.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "group_members", schema = "connectcg_be")
public class GroupMember {
    @EmbeddedId
    private GroupMemberId id;

    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'MEMBER'")
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Size(max = 20)
    @ColumnDefault("'ACCEPTED'")
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACCEPTED";

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "invited_by_id")
    private Integer invitedById;

}