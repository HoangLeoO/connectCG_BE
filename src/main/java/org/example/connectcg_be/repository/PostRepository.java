package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    Integer countByAuthorIdAndIsDeletedFalse(Integer authorId);

    long countByGroupIdAndStatus(Integer groupId, String status);

    java.util.List<Post> findAllByGroupIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(Integer groupId, String status);

    java.util.List<Post> findAllByGroupIdIsNullAndIsDeletedFalse();
}
