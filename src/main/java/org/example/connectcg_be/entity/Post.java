package org.example.connectcg_be.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Size(max = 20)
    @ColumnDefault("'PUBLIC'")
    @Column(name = "visibility", length = 20)
    private String visibility;

    @Size(max = 20)
    @ColumnDefault("'APPROVED'")
    @Column(name = "status", length = 20)
    private String status;

    @ColumnDefault("0")
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ColumnDefault("0")
    @Column(name = "comment_count")
    private Integer commentCount;

    @ColumnDefault("0")
    @Column(name = "react_count")
    private Integer reactCount;

    @ColumnDefault("0")
    @Column(name = "share_count")
    private Integer shareCount;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    /* ===== AI MODERATION ===== */

    @Size(max = 20)
    @ColumnDefault("'NOT_CHECKED'")
    @Column(name = "ai_status", length = 20)
    private String aiStatus;

    @Column(name = "ai_reason", columnDefinition = "TEXT")
    private String aiReason;

    @Column(name = "checked_at")
    private Instant checkedAt;

}
