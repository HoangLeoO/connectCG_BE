package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    @Query("SELECT g FROM Group g WHERE g.id NOT IN (SELECT gm.group.id FROM GroupMember gm WHERE gm.user.id = :userId) AND g.isDeleted = false")
    List<Group> findDiscoverGroups(@Param("userId") Integer userId);

    List<Group> findAllByIsDeletedFalse();

    @Query("SELECT g FROM Group g " +
            "LEFT JOIN UserProfile up ON g.owner.id = up.user.id WHERE " +
            "(LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(up.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "g.isDeleted = false")
    List<Group> searchByKeyword(@Param("keyword") String keyword);

    java.util.Optional<Group> findByIdAndIsDeletedFalse(Integer id);
}
