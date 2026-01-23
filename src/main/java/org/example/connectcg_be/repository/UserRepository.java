package org.example.connectcg_be.repository;

import java.util.Optional;

import org.example.connectcg_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // Tìm user nếu username trùng HOẶC email trùng
    Optional<User> findByUsernameOrEmail(String username, String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
