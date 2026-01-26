package org.example.connectcg_be.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.FriendRequestDTO;
import org.example.connectcg_be.entity.*;
import org.example.connectcg_be.repository.*;
import org.example.connectcg_be.service.FriendRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách các lời mời kết bạn đang ở trạng thái PENDING của người dùng hiện tại.
     * Sử dụng phân trang để hỗ trợ chức năng cuộn vô hạn (Infinite Scroll).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<FriendRequestDTO> getPendingRequests(Integer userId, Pageable pageable) {
        return friendRequestRepository.findByReceiverIdAndStatusOrderByCreatedAtDesc(userId, "PENDING", pageable)
                .map(this::mapToDTO);
    }

    /**
     * Chuyển đổi dữ liệu từ Entity (Database) sang DTO (Frontend).
     * Tại đây chúng ta "làm giàu" dữ liệu bằng cách tìm thêm tên hiển thị và ảnh đại diện của người gửi.
     */
    private FriendRequestDTO mapToDTO(FriendRequest request) {
        Integer senderId = request.getSender().getId();
        // Tìm profile để lấy tên đầy đủ
        UserProfile profile = userProfileRepository.findByUserId(senderId).orElse(null);
        
        String fullName = (profile != null) ? profile.getFullName() : null;
        String avatarUrl = getCurrentAvatar(senderId);

        return FriendRequestDTO.builder()
                .requestId(request.getId())
                .senderId(senderId)
                .senderUsername(request.getSender().getUsername())
                .senderFullName(fullName)
                .senderAvatarUrl(avatarUrl)
                .createdAt(request.getCreatedAt())
                .build();
    }

    /**
     * Lấy đường dẫn ảnh đại diện hiện tại của một người dùng.
     */
    private String getCurrentAvatar(Integer userId) {
        UserAvatar avatar = userAvatarRepository.findByUserIdAndIsCurrentTrue(userId);
        return (avatar != null && avatar.getMedia() != null) ? avatar.getMedia().getUrl() : null;
    }

    /**
     * Chấp nhận lời mời kết bạn.
     * Transaction đảm bảo trạng thái lời mời được cập nhật VÀ quan hệ bạn bè 2 chiều được tạo cùng lúc.
     */
    @Override
    @Transactional
    public void acceptFriendRequest(Integer requestId, Integer userId) {
        // Kiểm tra xem lời mời có tồn tại và có gửi cho đúng người dùng hiện tại không
        FriendRequest request = friendRequestRepository.findByIdAndReceiverId(requestId, userId)
                .orElseThrow(() -> new RuntimeException("Friend request not found or not for you"));

        // Chỉ chấp nhận các lời mời còn đang ở trạng thái PENDING
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request is already processed");
        }

        // 1. Cập nhật trạng thái lời mời thành ACCEPTED
        request.setStatus("ACCEPTED");
        request.setRespondedAt(Instant.now());
        friendRequestRepository.save(request);

        // 2. Tạo quan hệ bạn bè 2 chiều (Mutual Friendship)
        User sender = request.getSender();
        User receiver = request.getReceiver();

        // Kiểm tra xem hai người đã là bạn chưa để tránh tạo bản ghi trùng lặp
        if (!friendRepository.existsByUserIdAndFriendId(receiver.getId(), sender.getId())) {
            // Bản ghi: Người nhận kết bạn với người gửi
            Friend f1 = new Friend();
            FriendId fi1 = new FriendId();
            fi1.setUserId(receiver.getId());
            fi1.setFriendId(sender.getId());
            f1.setId(fi1);
            f1.setUser(receiver);
            f1.setFriend(sender);
            friendRepository.save(f1);

            // Bản ghi: Người gửi kết bạn với người nhận
            Friend f2 = new Friend();
            FriendId fi2 = new FriendId();
            fi2.setUserId(sender.getId());
            fi2.setFriendId(receiver.getId());
            f2.setId(fi2);
            f2.setUser(sender);
            f2.setFriend(receiver);
            friendRepository.save(f2);
        }
    }

    /**
     * Từ chối lời mời kết bạn.
     * Đơn giản là cập nhật trạng thái lời mời thành REJECTED.
     */
    @Override
    @Transactional
    public void rejectFriendRequest(Integer requestId, Integer userId) {
        FriendRequest request = friendRequestRepository.findByIdAndReceiverId(requestId, userId)
                .orElseThrow(() -> new RuntimeException("Friend request not found or not for you"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request is already processed");
        }

        request.setStatus("REJECTED");
        request.setRespondedAt(Instant.now());
        friendRequestRepository.save(request);
    }

    /**
     * Gửi một lời mời kết bạn mới.
     * Bao gồm nhiều bước kiểm tra an toàn: không tự kết bạn, đã là bạn, hoặc đã có lời mời chờ xử lý.
     */
    @Override
    @Transactional
    public void sendFriendRequest(Integer senderId, Integer receiverId) {
        // 1. Không cho phép tự kết bạn với chính mình
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("You cannot send a friend request to yourself");
        }

        // 2. Kiểm tra xem cả hai đã là bạn bè của nhau chưa
        if (friendRepository.existsByUserIdAndFriendId(senderId, receiverId)) {
            throw new RuntimeException("You are already friends");
        }

        // 3. Kiểm tra xem bạn đã gửi một lời mời khác cho người này mà chưa được xử lý không
        if (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(senderId, receiverId, "PENDING")) {
            throw new RuntimeException("A friend request is already pending");
        }

        // 4. Kiểm tra xem người đó đã gửi lời mời cho bạn chưa (Nếu rồi thì chỉ việc chấp nhận thôi)
        if (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(receiverId, senderId, "PENDING")) {
            throw new RuntimeException("This user has already sent you a friend request");
        }

        // 5. Nếu mọi điều kiện đều ổn, tiến hành tạo lời mời mới
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus("PENDING");
        request.setCreatedAt(Instant.now());

        friendRequestRepository.save(request);
    }
}
