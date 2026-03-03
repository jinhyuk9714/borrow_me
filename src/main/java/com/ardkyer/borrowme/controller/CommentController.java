package com.ardkyer.borrowme.controller;

import com.ardkyer.borrowme.entity.*;
import com.ardkyer.borrowme.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comments", description = "Comment management API")
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public CommentController(CommentService commentService, UserService userService, ProductService productService) {
        this.commentService = commentService;
        this.userService = userService;
        this.productService = productService;
    }

    @Getter @Setter
    public static class CommentRequest {
        private Long productId;
        private String content;
    }

    @Getter @Setter
    public static class CommentResponse {
        private Long id;
        private String content;
        private String username;
        private Date createdAt;
        private int likeCount;

        public CommentResponse(Comment comment) {
            this.id = comment.getId();
            this.content = comment.getContent();
            this.username = comment.getUser().getUsername();
            this.createdAt = java.sql.Timestamp.valueOf(comment.getCreatedAt());
            this.likeCount = comment.getLikeCount();
        }
    }

    @PostMapping
    @Operation(summary = "Add a new comment", description = "Creates a new comment for a product")
    public ResponseEntity<?> addComment(@RequestBody CommentRequest request, Authentication authentication) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        User user = userService.findByUsername(authentication.getName());
        Product product = productService.getProductById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent().trim());
        comment.setUser(user);
        comment.setProduct(product);
        comment.setLikeCount(0);

        Comment savedComment = commentService.addComment(comment);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "comment", new CommentResponse(savedComment)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a comment by ID")
    public ResponseEntity<?> getCommentById(@PathVariable Long id) {
        return commentService.getCommentById(id)
                .map(comment -> ResponseEntity.ok(new CommentResponse(comment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get comments for a product")
    public ResponseEntity<?> getCommentsByProduct(
            @PathVariable Long productId,
            Pageable pageable) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Page<Comment> comments = commentService.getCommentsByProduct(product, pageable);
        Page<CommentResponse> commentResponses = comments.map(CommentResponse::new);

        return ResponseEntity.ok(commentResponses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a comment")
    public ResponseEntity<?> updateComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request,
            Authentication authentication) {
        Comment existingComment = commentService.getCommentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        User currentUser = userService.findByUsername(authentication.getName());
        if (!existingComment.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "You can only update your own comments"
            ));
        }

        existingComment.setContent(request.getContent().trim());
        Comment updatedComment = commentService.updateComment(existingComment);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "comment", new CommentResponse(updatedComment)
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, Authentication authentication) {
        Comment comment = commentService.getCommentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        User currentUser = userService.findByUsername(authentication.getName());
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "You can only delete your own comments"
            ));
        }

        commentService.deleteComment(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

}