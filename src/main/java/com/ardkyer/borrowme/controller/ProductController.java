package com.ardkyer.borrowme.controller;

import com.amazonaws.services.s3.model.S3Object;
import com.ardkyer.borrowme.entity.Comment;
import com.ardkyer.borrowme.entity.Product;
import com.ardkyer.borrowme.entity.User;
import com.ardkyer.borrowme.entity.Reservation;
import com.ardkyer.borrowme.entity.Hashtag;
import com.ardkyer.borrowme.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management API")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserService userService;
    private final ReservationService reservationService;
    private final CommentService commentService;
    private final FollowService followService;

    // Request/Response DTOs
    @Getter @Setter
    public static class ReservationRequest {
        private Integer quantity;
    }

    @Getter @Setter
    public static class ProductResponse {
        private Long id;
        private String title;
        private String description;
        private Integer totalQuantity;
        private Integer availableQuantity;
        private String status;
        private UserInfo user;
        private boolean isFollowedByCurrentUser;
        private List<String> hashtags;
        private String imageUrl;

        @Getter @Setter
        public static class UserInfo {
            private Long id;
            private String username;
        }
    }

    @GetMapping
    @Operation(summary = "List all products", description = "Retrieves a list of all available products")
    public ResponseEntity<List<ProductResponse>> getProducts(Authentication authentication) {
        List<Product> products = productService.getAllProductsWithDetails();

        Set<Long> followedUserIds = Collections.emptySet();
        if (authentication != null) {
            User currentUser = userService.findByUsername(authentication.getName());
            List<User> owners = products.stream().map(Product::getUser).distinct().collect(Collectors.toList());
            followedUserIds = followService.getFollowedUserIds(currentUser, owners);
        }

        final Set<Long> finalFollowedIds = followedUserIds;
        List<ProductResponse> response = products.stream()
                .map(product -> convertToProductResponse(product, finalFollowedIds))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details", description = "Retrieves details of a specific product")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "ID of the product") @PathVariable Long id,
            Authentication authentication) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(convertToProductResponse(product, authentication)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a product", description = "Creates a new product with image")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile file,
            @RequestParam("totalQuantity") Integer totalQuantity,
            @RequestParam(value = "hashtags", required = false) String hashtags,
            Authentication authentication) throws IOException {

        if (!file.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }

        if (totalQuantity == null || totalQuantity <= 0) {
            return ResponseEntity.badRequest().build();
        }

        User currentUser = userService.findByUsername(authentication.getName());
        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setUser(currentUser);
        product.setTotalQuantity(totalQuantity);
        product.setAvailableQuantity(totalQuantity);
        product.setReservationStatus(Product.ReservationStatus.AVAILABLE);

        Set<String> hashtagSet = extractHashtags(description, hashtags);
        product = productService.uploadProduct(product, file, hashtagSet);

        return ResponseEntity.ok(convertToProductResponse(product, authentication));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product", description = "Updates an existing product")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile file,
            @RequestParam("totalQuantity") Integer totalQuantity,
            Authentication authentication) throws IOException {

        Optional<Product> productOptional = productService.getProductById(id);
        if (productOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOptional.get();
        User currentUser = userService.findByUsername(authentication.getName());

        if (!product.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        product.setTitle(title);
        product.setDescription(description);
        product.setTotalQuantity(totalQuantity);

        if (file != null && !file.isEmpty()) {
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }
            product = productService.updateProductWithImage(product, file);
        } else {
            product = productService.updateProduct(product);
        }

        return ResponseEntity.ok(convertToProductResponse(product, authentication));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product", description = "Deletes an existing product")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Reserve a product", description = "Creates a reservation for a product")
    public ResponseEntity<?> createReservation(
            @PathVariable Long id,
            @RequestBody ReservationRequest request,
            Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Cannot reserve your own product");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }

        Reservation reservation = reservationService.reserve(product, currentUser, request.getQuantity());

        return ResponseEntity.ok(Map.of(
                "reservationId", reservation.getId(),
                "remainingQuantity", reservation.getProduct().getAvailableQuantity()
        ));
    }

    @GetMapping("/images/{fileName:.+}")
    @Operation(summary = "Get product image", description = "Retrieves a product image")
    public ResponseEntity<InputStreamResource> getProductImage(
            @Parameter(description = "Name of the image file")
            @PathVariable String fileName) {
        S3Object s3Object = productService.getProductFile(fileName);

        HttpHeaders headers = new HttpHeaders();
        String contentType = s3Object.getObjectMetadata().getContentType();
        headers.setContentType(MediaType.parseMediaType(
                contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE));
        headers.setContentLength(s3Object.getObjectMetadata().getContentLength());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(s3Object.getObjectContent()));
    }

    private ProductResponse convertToProductResponse(Product product, Authentication authentication) {
        Set<Long> followedIds = Collections.emptySet();
        if (authentication != null) {
            User currentUser = userService.findByUsername(authentication.getName());
            followedIds = followService.getFollowedUserIds(currentUser, List.of(product.getUser()));
        }
        return convertToProductResponse(product, followedIds);
    }

    private ProductResponse convertToProductResponse(Product product, Set<Long> followedUserIds) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setTitle(product.getTitle());
        response.setDescription(product.getDescription());
        response.setTotalQuantity(product.getTotalQuantity());
        response.setAvailableQuantity(product.getAvailableQuantity());
        response.setStatus(product.getReservationStatus().name());

        List<String> hashtagStrings = product.getHashtags().stream()
                .map(Hashtag::getName)
                .collect(Collectors.toList());
        response.setHashtags(hashtagStrings);

        response.setImageUrl("/api/products/images/" + product.getImageUrl());

        ProductResponse.UserInfo userInfo = new ProductResponse.UserInfo();
        userInfo.setId(product.getUser().getId());
        userInfo.setUsername(product.getUser().getUsername());
        response.setUser(userInfo);

        response.setFollowedByCurrentUser(followedUserIds.contains(product.getUser().getId()));

        return response;
    }

    private Set<String> extractHashtags(String description, String additionalHashtags) {
        Set<String> hashtagSet = Arrays.stream(description.split(" "))
                .filter(tag -> tag.startsWith("#"))
                .collect(Collectors.toSet());

        if (additionalHashtags != null && !additionalHashtags.trim().isEmpty()) {
            hashtagSet.addAll(Arrays.stream(additionalHashtags.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }

        return hashtagSet;
    }
}