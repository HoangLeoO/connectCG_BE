package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    Integer countByAuthorIdAndIsDeletedFalse(Integer authorId);

    long countByGroupIdAndStatus(Integer groupId, String status);
    @Query("""
            select p from Post p where p.isDeleted = false and p.status = 'APPROVED' and p.author.id = :userId order by p.createdAt desc
            """)
    List<Post> findAllByAuthorIdAndStatusApproved(@Param("userId") Integer userId);

    java.util.List<Post> findAllByGroupIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(Integer groupId, String status);

    java.util.List<Post> findAllByGroupIdIsNullAndIsDeletedFalse();

    java.util.List<Post> findAllByGroupIdIsNullAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(String status);

    java.util.List<Post> findAllByGroupIdIsNullAndStatusAndAiStatusAndIsDeletedFalseOrderByCreatedAtDesc(String status,
                                                                                                         String aiStatus);

    @Query("""
              select p from Post p
              where p.isDeleted = false
                and p.status = 'APPROVED'
                and (
                  (p.group.id in :groupIds)
                  or
                  (p.group is null and (
                      p.author.id = :userId
                      or (p.author.id in :friendIds and p.visibility in ('PUBLIC','FRIENDS'))
                      or (p.visibility = 'PUBLIC')
                  ))
                )
              order by p.createdAt desc
            """)
    List<Post> findNewsfeedPosts(@Param("userId") Integer userId,
                                 @Param("friendIds") List<Integer> friendIds,
                                 @Param("groupIds") List<Integer> groupIds);
}
