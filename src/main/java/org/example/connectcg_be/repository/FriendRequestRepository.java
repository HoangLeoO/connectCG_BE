package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {
    // Tìm các lời mời đang chờ xử lý của người nhận, hỗ trợ phân trang cho infinite scroll
    Page<FriendRequest> findByReceiverIdAndStatusOrderByCreatedAtDesc(Integer receiverId, String status, Pageable pageable);

    // Tìm lời mời theo ID và người nhận (để bảo mật khi accept/reject)
    Optional<FriendRequest> findByIdAndReceiverId(Integer id, Integer receiverId);

    // Kiểm tra xem đã có lời mời PENDING giữa 2 người chưa
    boolean existsBySenderIdAndReceiverIdAndStatus(Integer senderId, Integer receiverId, String status);
}
