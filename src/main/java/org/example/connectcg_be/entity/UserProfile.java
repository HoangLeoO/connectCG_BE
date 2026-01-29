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
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 50)
    @Column(name = "city_code", length = 50)
    private String cityCode;

    @Size(max = 100)
    @Column(name = "city_name", length = 100)
    private String cityName;

    @Size(max = 100)
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 20)
    @Column(name = "gender", length = 20)
    private String gender;

    @Size(max = 255)
    @Column(name = "bio")
    private String bio;

    @Size(max = 100)
    @Column(name = "occupation", length = 100)
    private String occupation;

    @Size(max = 20)
    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Size(max = 20)
    @Column(name = "looking_for", length = 20)
    private String lookingFor;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}
