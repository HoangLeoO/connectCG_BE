package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    long countByGroupIdAndStatus(Integer groupId, String status);
}
