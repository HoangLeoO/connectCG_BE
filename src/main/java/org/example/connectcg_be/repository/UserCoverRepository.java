package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.UserCover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCoverRepository extends JpaRepository<UserCover, Integer> {
    Optional<UserCover> findByUserIdAndIsCurrentTrue(Integer userId);
}
