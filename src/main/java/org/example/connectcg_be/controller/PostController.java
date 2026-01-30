package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.CreatePostRequest;
import org.example.connectcg_be.dto.GroupPostDTO;
import org.example.connectcg_be.entity.Post;
import org.example.connectcg_be.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final org.example.connectcg_be.service.PostService postService;

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GroupPostDTO>> getNewsfeedPosts(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(postService.getNewsfeedPosts(currentUser.getId()));
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GroupPostDTO>> getUserProfilePosts(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(postService.getPostsByUserId(id));
    }

    @GetMapping("/public/homepage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Post>> getPublicHomepagePosts() {
        return ResponseEntity.ok(postService.getHomepagePostsByStatus("APPROVED"));
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<org.example.connectcg_be.dto.GroupPostDTO>> getPendingHomepagePosts() {
        return ResponseEntity.ok(postService.getPendingHomepagePosts());
    }

    @GetMapping("/admin/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<org.example.connectcg_be.dto.GroupPostDTO>> getAuditHomepagePosts() {
        return ResponseEntity.ok(postService.getAuditHomepagePosts());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @RequestParam(defaultValue = "false") boolean skipAiCheck,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Post createdPost = postService.createPost(request, skipAiCheck, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approvePost(@PathVariable Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        postService.approvePost(id, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> updatePost(
            @PathVariable Integer id,
            @Valid @RequestBody CreatePostRequest request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Post updatedPost = postService.updatePost(id, request, userPrincipal.getId());
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        postService.rejectPost(id, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }
}
