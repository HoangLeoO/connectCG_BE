package org.example.connectcg_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "user_public_keys")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPublicKey {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "public_key", columnDefinition = "TEXT", nullable = false)
    private String publicKey;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}
