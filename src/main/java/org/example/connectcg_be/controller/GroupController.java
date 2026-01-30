package org.example.connectcg_be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.CreateGroup;
import org.example.connectcg_be.dto.GroupDTO;
import org.example.connectcg_be.dto.TungGroupMemberDTO;
import org.example.connectcg_be.service.GroupMemberService;
import org.example.connectcg_be.service.GroupService;
import org.example.connectcg_be.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.example.connectcg_be.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin("*")
public class GroupController {
    @Autowired
    GroupService groupService;
    @Autowired
    MediaService mediaService;
    @Autowired
    GroupMemberService groupMemberService;
    @Autowired
    org.example.connectcg_be.service.PostService postService;

    @GetMapping("/{id}/posts/pending")
    @PreAuthorize("hasRole('ADMIN') or @groupSecurity.isGroupAdmin(#id)")
    public List<org.example.connectcg_be.dto.GroupPostDTO> getPendingPosts(@PathVariable("id") Integer id) {
        return postService.getPendingPosts(id);
    }

    @GetMapping("/{id}/posts")
    @PreAuthorize("hasRole('ADMIN') or @groupSecurity.isGroupMember(#id) or @groupSecurity.isPublicGroup(#id)")
    public List<org.example.connectcg_be.dto.GroupPostDTO> getGroupPosts(@PathVariable("id") Integer id) {
        return postService.getApprovedPosts(id);
    }

    @PostMapping("/{id}/posts/{postId}/approve")
    @PreAuthorize("hasRole('ADMIN') or @groupSecurity.isGroupAdmin(#id)")
    public ResponseEntity<Void> approvePost(@PathVariable("id") Integer id, @PathVariable("postId") Integer postId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        postService.approvePost(postId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/posts/{postId}/reject")
    @PreAuthorize("hasRole('ADMIN') or @groupSecurity.isGroupAdmin(#id)")
    public ResponseEntity<Void> rejectPost(@PathVariable("id") Integer id, @PathVariable("postId") Integer postId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        postService.rejectPost(postId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public org.springframework.data.domain.Page<GroupDTO> getAll(
            @org.springframework.data.web.PageableDefault(size = 9) org.springframework.data.domain.Pageable pageable) {
        return groupService.findAllGroups(pageable);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> create(@Valid @RequestBody CreateGroup request, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.addGroup(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/my-groups")
    @PreAuthorize("isAuthenticated()")
    public org.springframework.data.domain.Page<GroupDTO> getMyGroups(
            @org.springframework.data.web.PageableDefault(size = 9) org.springframework.data.domain.Pageable pageable,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.findMyGroups(userPrincipal.getId(), pageable);
    }

    @GetMapping("/discover")
    @PreAuthorize("isAuthenticated()")
    public org.springframework.data.domain.Page<GroupDTO> getDiscoverGroups(
            @org.springframework.data.web.PageableDefault(size = 9) org.springframework.data.domain.Pageable pageable,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.findDiscoverGroups(userPrincipal.getId(), pageable);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public org.springframework.data.domain.Page<GroupDTO> searchGroups(
            @RequestParam("name") String name,
            @org.springframework.data.web.PageableDefault(size = 9) org.springframework.data.domain.Pageable pageable,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.searchGroups(name, userPrincipal.getId(), pageable);
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("isAuthenticated()")
    public List<TungGroupMemberDTO> getMembers(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.getMembers(id, userPrincipal.getId());
    }

    @DeleteMapping("/{id}/leave")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> leaveGroup(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            groupService.leaveGroup(id, userPrincipal.getId());
            return ResponseEntity.ok("Left group successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @groupSecurity.isGroupAdmin(#id)")
    public ResponseEntity<String> deleteGroup(@PathVariable("id") Integer id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            Integer currentUserId = userPrincipal.getId();
            groupService.deleteGroup(id, userPrincipal.getId());
            return ResponseEntity.ok("Deleted group successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/invite")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @groupSecurity.isGroupMember(#id))")
    public ResponseEntity<Void> inviteMembers(@PathVariable("id") Integer id, @RequestBody List<Integer> userIds,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.inviteMembers(id, userIds, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public GroupDTO getById(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.findById(id, userPrincipal.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @groupSecurity.isGroupAdmin(#id)")
    public ResponseEntity<Void> update(@PathVariable("id") Integer id, @Valid @RequestBody CreateGroup request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.updateGroup(id, request, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> acceptInvitation(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.acceptInvitation(id, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/decline")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> declineInvitation(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.declineInvitation(id, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invitations")
    @PreAuthorize("isAuthenticated()")
    public List<GroupDTO> getPendingInvitations(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.findPendingInvitations(userPrincipal.getId());
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> joinGroup(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            groupService.joinGroup(id, userPrincipal.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/approve/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @groupSecurity.isGroupAdmin(#id)")
    public ResponseEntity<String> approveJoinRequest(@PathVariable("id") Integer id,
            @PathVariable("userId") Integer userId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            groupService.approveJoinRequest(id, userId, userPrincipal.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @groupSecurity.isGroupAdmin(#id)")
    public ResponseEntity<String> rejectJoinRequest(@PathVariable("id") Integer id,
            @PathVariable("userId") Integer userId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            groupService.rejectJoinRequest(id, userId, userPrincipal.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/requests")
    @PreAuthorize("hasRole('ADMIN') or @groupSecurity.isGroupAdmin(#id)")
    public List<TungGroupMemberDTO> getPendingJoinRequests(@PathVariable("id") Integer id,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.getPendingJoinRequests(id, userPrincipal.getId());
    }

    @DeleteMapping("/{id}/kick/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @groupSecurity.isGroupAdmin(#id)")
    public ResponseEntity<String> kickMember(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            groupService.kickMember(id, userId, userPrincipal.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/transfer-ownership")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> transferOwnership(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Integer> request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            Integer newOwnerId = request.get("newOwnerId");
            if (newOwnerId == null) {
                return ResponseEntity.badRequest().body("Vui lòng chọn chủ sở hữu mới");
            }

            groupService.transferOwnershipAndLeave(id, newOwnerId, userPrincipal.getId());
            return ResponseEntity.ok("Đã chuyển quyền sở hữu và rời nhóm thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/members/{userId}/role")
    @PreAuthorize("isAuthenticated() and @groupSecurity.isGroupAdmin(#id)")
    public ResponseEntity<String> updateMemberRole(
            @PathVariable("id") Integer id,
            @PathVariable("userId") Integer userId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            String newRole = request.get("role");
            groupService.updateMemberRole(id, userId, newRole, userPrincipal.getId());
            return ResponseEntity.ok("Cập nhật vai trò thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
