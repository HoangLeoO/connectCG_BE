package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

  // Atomic update methods to prevent deadlocks
  @Modifying
  @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
  void incrementCommentCount(@Param("postId") Integer postId);

  @Modifying
  @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
  void decrementCommentCount(@Param("postId") Integer postId);

  @Modifying
  @Query("UPDATE Post p SET p.reactCount = p.reactCount + 1 WHERE p.id = :postId")
  void incrementReactCount(@Param("postId") Integer postId);

  @Modifying
  @Query("UPDATE Post p SET p.reactCount = p.reactCount - 1 WHERE p.id = :postId AND p.reactCount > 0")
  void decrementReactCount(@Param("postId") Integer postId);

  Integer countByAuthorIdAndIsDeletedFalse(Integer authorId);

  Integer countByAuthorIdAndStatusAndIsDeletedFalse(Integer authorId, String status);

  long countByGroupIdAndStatus(Integer groupId, String status);

  List<Post> findAllByAuthorIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(Integer authorId, String status);

  List<Post> findAllByAuthorIdAndGroupIdAndStatusAndIsDeletedFalse(Integer authorId, Integer groupId, String status);

  List<Post> findAllByAuthorIdAndStatusAndIsDeletedFalse(Integer authorId, String status);

  java.util.List<Post> findAllByGroupIdAndStatusAndIsDeletedFalseOrderByIsPinnedDescPinnedAtDescCreatedAtDesc(
      Integer groupId, String status);

  java.util.List<Post> findAllByGroupIdIsNullAndIsDeletedFalse();

  java.util.List<Post> findAllByGroupIdIsNullAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(String status);

  org.springframework.data.domain.Page<Post> findAllByGroupIdIsNullAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
      String status, org.springframework.data.domain.Pageable pageable);

  java.util.List<Post> findAllByGroupIdIsNullAndStatusAndAiStatusAndIsDeletedFalseOrderByCreatedAtDesc(String status,
      String aiStatus);

  org.springframework.data.domain.Page<Post> findAllByGroupIdIsNullAndStatusAndAiStatusAndIsDeletedFalseOrderByCreatedAtDesc(
      String status, String aiStatus, org.springframework.data.domain.Pageable pageable);

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

  @Query(value = """
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
      """, countQuery = """
        select count(p) from Post p
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
      """)
  org.springframework.data.domain.Page<Post> findNewsfeedPosts(@Param("userId") Integer userId,
      @Param("friendIds") List<Integer> friendIds,
      @Param("groupIds") List<Integer> groupIds,
      org.springframework.data.domain.Pageable pageable);
}
