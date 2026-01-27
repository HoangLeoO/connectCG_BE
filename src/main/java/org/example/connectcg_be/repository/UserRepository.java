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

    long countByRoleAndIsDeletedFalseAndIsLockedFalse(String role);

    // Tìm kiếm phân trang cho Admin với bộ lọc Role và chỉ theo FullName (không
    // theo username/email)
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u " +
            "LEFT JOIN UserProfile up ON up.user = u WHERE " +
            "(:role IS NULL OR :role = '' OR u.role = :role) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(up.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    org.springframework.data.domain.Page<User> findByFilters(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("role") String role,
            org.springframework.data.domain.Pageable pageable);
}
