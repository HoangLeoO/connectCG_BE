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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public List<GroupDTO> getAll() {
        return groupService.findAllGroups();
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateGroup request, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.addGroup(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/my-groups")
    public List<GroupDTO> getMyGroups(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.findMyGroups(userPrincipal.getId());
    }

    @GetMapping("/discover")
    public List<GroupDTO> getDiscoverGroups(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.findDiscoverGroups(userPrincipal.getId());
    }

    @GetMapping("/search")
    public List<GroupDTO> searchGroups(@RequestParam("name") String name, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.searchGroups(name, userPrincipal.getId());
    }

    @GetMapping("/{id}/members")
    public List<TungGroupMemberDTO> getMembers(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.getMembers(id, userPrincipal.getId());
    }

    @DeleteMapping("/{id}/leave")
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
    public ResponseEntity<String> deleteGroup(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            groupService.deleteGroup(id, userPrincipal.getId());
            return ResponseEntity.ok("Deleted group successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<Void> inviteMembers(@PathVariable("id") Integer id, @RequestBody List<Integer> userIds,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.inviteMembers(id, userIds, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public GroupDTO getById(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.findById(id, userPrincipal.getId());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Integer id, @Valid @RequestBody CreateGroup request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.updateGroup(id, request, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Void> acceptInvitation(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.acceptInvitation(id, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<Void> declineInvitation(@PathVariable("id") Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        groupService.declineInvitation(id, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invitations")
    public List<GroupDTO> getPendingInvitations(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.findPendingInvitations(userPrincipal.getId());
    }

    @PostMapping("/{id}/join")
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
    public List<TungGroupMemberDTO> getPendingJoinRequests(@PathVariable("id") Integer id,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return groupService.getPendingJoinRequests(id, userPrincipal.getId());
    }

    @DeleteMapping("/{id}/kick/{userId}")
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
}
