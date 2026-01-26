package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatar, Integer> {
    UserAvatar findByUserIdAndIsCurrentTrue(Integer userId);
}
