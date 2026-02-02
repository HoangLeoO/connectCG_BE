package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    // Lấy tất cả comment của 1 bài viết, sắp xếp theo thời gian
    List<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Integer postId);

    // Đếm số comment của 1 bài viết
    long countByPostIdAndIsDeletedFalse(Integer postId);
}
