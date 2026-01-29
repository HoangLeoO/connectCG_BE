package org.example.connectcg_be.repository;

import org.example.connectcg_be.dto.MemberSearchResponse;
import org.example.connectcg_be.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    Optional<UserProfile> findByUserId(Integer userId);
    boolean existsByUserId(Integer id);

    @Query("SELECT new org.example.connectcg_be.dto.MemberSearchResponse(" +
           "u.id, u.username, p.fullName, p.cityName, p.gender, p.maritalStatus, p.lookingFor, " +
           "(CASE WHEN f.id.userId IS NOT NULL THEN true ELSE false END), " +
           "(CASE WHEN fr.id IS NOT NULL THEN true ELSE false END), " +
           "fr.id, " +
           "(CASE WHEN fr.receiver.id = :currentUserId THEN true ELSE false END)) " +
           "FROM UserProfile p " +
           "JOIN p.user u " +
           "LEFT JOIN Friend f ON (f.user.id = :currentUserId AND f.friend.id = u.id) " +
           "LEFT JOIN FriendRequest fr ON ((fr.sender.id = :currentUserId AND fr.receiver.id = u.id AND fr.status = 'PENDING') OR (fr.sender.id = u.id AND fr.receiver.id = :currentUserId AND fr.status = 'PENDING')) " +
           "WHERE (:keyword IS NULL OR lower(p.fullName) LIKE lower(concat('%', :keyword, '%')) OR lower(u.username) LIKE lower(concat('%', :keyword, '%'))) " +
           "AND (:gender IS NULL OR p.gender = :gender) " +
           "AND (:cityCode IS NULL OR p.cityCode = :cityCode) " +
           "AND (:maritalStatus IS NULL OR p.maritalStatus = :maritalStatus) " +
           "AND (:lookingFor IS NULL OR p.lookingFor = :lookingFor) " +
           "AND u.id != :currentUserId " +
           "ORDER BY (CASE WHEN f.id.userId IS NOT NULL THEN 1 ELSE 0 END) DESC, p.fullName ASC")
    Page<MemberSearchResponse> searchMembers(
            @Param("currentUserId") Integer currentUserId,
            @Param("keyword") String keyword,
            @Param("gender") String gender,
            @Param("cityCode") String cityCode,
            @Param("maritalStatus") String maritalStatus,
            @Param("lookingFor") String lookingFor,
            Pageable pageable
    );
}
