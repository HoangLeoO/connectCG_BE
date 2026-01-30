package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    @Query("SELECT g FROM Group g " +
            "LEFT JOIN GroupMember gm ON g.id = gm.group.id AND gm.status = 'ACCEPTED' " +
            "WHERE g.id NOT IN (SELECT gm2.group.id FROM GroupMember gm2 WHERE gm2.user.id = :userId AND gm2.status IN ('ACCEPTED', 'PENDING')) "
            +
            "AND g.isDeleted = false " +
            "GROUP BY g.id " +
            "ORDER BY COUNT(gm.user.id) DESC, g.createdAt DESC")
    org.springframework.data.domain.Page<Group> findDiscoverGroups(@Param("userId") Integer userId,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT g FROM Group g " +
            "JOIN GroupMember gm ON g.id = gm.group.id " +
            "WHERE gm.user.id = :userId AND gm.status = 'ACCEPTED' AND g.isDeleted = false " +
            "ORDER BY g.createdAt DESC")
    org.springframework.data.domain.Page<Group> findMyGroups(@Param("userId") Integer userId,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT g FROM Group g " +
            "LEFT JOIN UserProfile up ON g.owner.id = up.user.id " +
            "LEFT JOIN GroupMember gm ON g.id = gm.group.id AND gm.status = 'ACCEPTED' " +
            "WHERE (LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(up.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "g.isDeleted = false " +
            "GROUP BY g.id " +
            "ORDER BY COUNT(gm.user.id) DESC, g.createdAt DESC")
    org.springframework.data.domain.Page<Group> searchByKeyword(@Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);

    List<Group> findAllByIsDeletedFalse();

    java.util.Optional<Group> findByIdAndIsDeletedFalse(Integer id);

}
