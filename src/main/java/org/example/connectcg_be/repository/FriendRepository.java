package org.example.connectcg_be.repository;

import org.example.connectcg_be.dto.FriendDTO;
import org.example.connectcg_be.entity.Friend;
import org.example.connectcg_be.entity.FriendId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, FriendId> {

    @Query("SELECT new org.example.connectcg_be.dto.FriendDTO(" +
            "f.friend.id, " +
            "p.fullName, " +
            "f.friend.username, " +
            "p.gender, " +
            "c.name, " +
            "m.url, " +
            "p.dateOfBirth, " +
            "p.occupation) " +
            "FROM Friend f " +
            "LEFT JOIN UserProfile p ON p.user = f.friend " +
            "LEFT JOIN p.city c " +
            "LEFT JOIN UserAvatar ua ON ua.user = f.friend AND ua.isCurrent = true " +
            "LEFT JOIN ua.media m " +
            "WHERE f.user.id = :userId " +
            "AND (:name IS NULL OR p.fullName LIKE %:name% OR f.friend.username LIKE %:name%) " +
            "AND (:gender IS NULL OR p.gender = :gender) " +
            "AND (:cityId IS NULL OR c.id = :cityId)")
    Page<FriendDTO> searchFriends(
            @Param("userId") Integer userId,
            @Param("name") String name,
            @Param("gender") String gender,
            @Param("cityId") Integer cityId,
            Pageable pageable);

    boolean existsByUserIdAndFriendId(Integer userId, Integer friendId);

    Integer countByUserId(Integer userId);
}
