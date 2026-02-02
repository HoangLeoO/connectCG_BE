package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.CommentDTO;
import org.example.connectcg_be.dto.CreateCommentRequest;

import java.util.List;

public interface CommentService {
    // Lấy danh sách comment dạng cây
    List<CommentDTO> getCommentsByPostId(Integer postId);

    // Tạo comment mới
    CommentDTO createComment(Integer postId, Integer userId, CreateCommentRequest request);

    // Xóa comment
    void deleteComment(Integer commentId, Integer userId);
}
