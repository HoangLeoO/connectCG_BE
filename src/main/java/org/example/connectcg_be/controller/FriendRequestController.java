package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.FriendRequestDTO;
import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.FriendRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/friend-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    /**
     * API Lấy danh sách lời mời kết bạn đã nhận (Đang ở trạng thái chờ).
     * @param currentUser Thông tin user đang đăng nhập (lấy từ Token).
     */
    @GetMapping
    public ResponseEntity<Page<FriendRequestDTO>> getPendingRequests(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Integer userId = (currentUser != null) ? currentUser.getId() : null;
        Pageable pageable = PageRequest.of(page, size);
        Page<FriendRequestDTO> requests = friendRequestService.getPendingRequests(userId, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * API Chấp nhận một lời mời kết bạn.
     * @param requestId ID của bản ghi lời mời (FriendRequest ID).
     */
    @PostMapping("/{requestId}/accept")
    public ResponseEntity<Void> acceptRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Integer requestId) {
        
        Integer userId = (currentUser != null) ? currentUser.getId() : null;
        friendRequestService.acceptFriendRequest(requestId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * API Từ chối một lời mời kết bạn.
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<Void> rejectRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Integer requestId) {
        
        Integer userId = (currentUser != null) ? currentUser.getId() : null;
        friendRequestService.rejectFriendRequest(requestId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * API Gửi một lời mời kết bạn mới.
     * @param receiverId ID của người mà bạn muốn kết bạn.
     */
    @PostMapping("/send/{receiverId}")
    public ResponseEntity<Void> sendRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Integer receiverId) {
        
        Integer userId = (currentUser != null) ? currentUser.getId() : null;
        friendRequestService.sendFriendRequest(userId, receiverId);
        return ResponseEntity.ok().build();
    }
}
