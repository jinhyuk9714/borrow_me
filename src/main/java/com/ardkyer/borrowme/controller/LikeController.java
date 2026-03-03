package com.ardkyer.borrowme.controller;

import com.ardkyer.borrowme.entity.*;
import com.ardkyer.borrowme.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@Tag(name = "Like", description = "Like management API")
public class LikeController {
    private final LikeService likeService;
    private final CustomUserDetailsService customUserDetailsService;
    private final ProductService productService;

    @Autowired
    public LikeController(LikeService likeService, CustomUserDetailsService customUserDetailsService, ProductService productService) {
        this.likeService = likeService;
        this.customUserDetailsService = customUserDetailsService;
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Toggle like on a product", description = "Likes or unlikes a product for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully toggled like", content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<Map<String, Object>> toggleLike(
            @Parameter(description = "Product ID to like/unlike", required = true) @RequestBody Map<String, Long> payload,
            Authentication authentication) {
        User user = customUserDetailsService.loadUserEntityByUsername(authentication.getName());
        Product product = productService.getProductById(payload.get("productId"))
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        boolean liked = likeService.toggleLike(user, product);
        long likeCount = likeService.getLikeCount(product);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove like from a product", description = "Removes the like from a product for the authenticated user")
    @ApiResponse(responseCode = "204", description = "Successfully removed like")
    public ResponseEntity<Void> removeLike(
            @Parameter(description = "ID of the product to remove like from", required = true) @PathVariable Long productId,
            Authentication authentication) {
        User user = customUserDetailsService.loadUserEntityByUsername(authentication.getName());
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        likeService.removeLike(user, product);
        return ResponseEntity.noContent().build();
    }
}