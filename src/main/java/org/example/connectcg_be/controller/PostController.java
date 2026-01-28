package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.CreatePostRequest;
import org.example.connectcg_be.entity.Post;
import org.example.connectcg_be.repository.PostRepository;
import org.example.connectcg_be.security.UserPrincipal;
import org.example.connectcg_be.service.GeminiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final GeminiService geminiService;
    private final org.example.connectcg_be.service.PostService postService;

    @GetMapping("/admin/homepage")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<Post>> getHomepagePosts() {
        return ResponseEntity.ok(postRepository.findAllByGroupIdIsNullAndIsDeletedFalse());
    }

    @PostMapping("/admin/{id}/check-ai")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Map<String, String>> checkPostWithAI(@PathVariable Integer id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        String result = geminiService.checkPostContent(post.getContent());
        return ResponseEntity.ok(Map.of("result", result));
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
}
