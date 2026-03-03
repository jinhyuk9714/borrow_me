package com.ardkyer.borrowme.controller;

import com.ardkyer.borrowme.entity.RecentSearch;
import com.ardkyer.borrowme.entity.User;
import com.ardkyer.borrowme.entity.Product;
import com.ardkyer.borrowme.entity.Exercise;
import com.ardkyer.borrowme.entity.Hashtag;
import com.ardkyer.borrowme.service.ExerciseService;
import com.ardkyer.borrowme.service.RecentSearchService;
import com.ardkyer.borrowme.service.UserService;
import com.ardkyer.borrowme.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Search management API")
@RequiredArgsConstructor
public class SearchController {

    private final ProductService productService;
    private final ExerciseService exerciseService;
    private final RecentSearchService recentSearchService;
    private final UserService userService;

    @Getter @Setter
    public static class SearchResponse {
        private List<ProductDto> products;
        private List<ExerciseDto> exercises;
        private List<RecentSearchDto> recentSearches;

        @Getter @Setter
        public static class ProductDto {
            private Long id;
            private String title;
            private String description;
            private String imageUrl;
            private List<String> hashtags;
            private UserDto user;

            @Getter @Setter
            public static class UserDto {
                private Long id;
                private String username;
            }
        }

        @Getter @Setter
        public static class ExerciseDto {
            private Long id;
            private String name;
            private List<String> hashtags; // Changed from Set to List for better JSON serialization
        }

        @Getter @Setter
        public static class RecentSearchDto {
            private String keyword;           // searchTerm -> keyword로 변경
            private LocalDateTime searchTime; // createdAt -> searchTime으로 변경
        }
    }

    @GetMapping
    @Operation(
            summary = "Search for products",
            description = "Search for products based on the query, which can be an exercise name or a hashtag"
    )
    public ResponseEntity<SearchResponse> search(
            @Parameter(description = "The search query (exercise name or hashtag)")
            @RequestParam(value = "query", required = false) String query,
            @Parameter(description = "The source of the search query (search or exercise)")
            @RequestParam(value = "source", required = false) String source,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        SearchResponse response = new SearchResponse();

        // 검색 결과 처리
        if (query != null && !query.isEmpty()) {
            Set<String> hashtags = exerciseService.getHashtagsByExerciseName(query);
            List<Product> products;

            if (hashtags.isEmpty()) {
                products = productService.searchProducts(query);
            } else {
                products = productService.searchProductsByHashtags(hashtags);
            }

            response.setProducts(convertToProductDtos(products));

            // 검색창에서 입력된 검색어만 저장
            if (!"exercise".equals(source)) {
                recentSearchService.addOrUpdateRecentSearch(user, query);
            }
        }

        // 운동 목록 설정
        response.setExercises(convertToExerciseDtos(exerciseService.getAllExercises()));

        // 최근 검색 기록 설정
        response.setRecentSearches(convertToRecentSearchDtos(recentSearchService.getRecentSearches(user)));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/recent")
    @Operation(
            summary = "Delete recent search query",
            description = "Delete a specific recent search query of the logged-in user"
    )
    public ResponseEntity<Void> deleteRecentSearch(
            @Parameter(description = "The search query to delete")
            @RequestParam String query,
            Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        recentSearchService.deleteRecentSearch(user, query);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/recent/all")
    @Operation(
            summary = "Delete all recent search queries",
            description = "Delete all recent search queries of the logged-in user"
    )
    public ResponseEntity<Void> deleteAllRecentSearches(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        recentSearchService.deleteAllRecentSearches(user);
        return ResponseEntity.ok().build();
    }

    // Helper methods to convert entities to DTOs
    private List<SearchResponse.ProductDto> convertToProductDtos(List<Product> products) {
        if (products == null) return List.of();

        return products.stream()
                .map(product -> {
                    SearchResponse.ProductDto dto = new SearchResponse.ProductDto();
                    dto.setId(product.getId());
                    dto.setTitle(product.getTitle());
                    dto.setDescription(product.getDescription());
                    dto.setImageUrl("/api/products/images/" + product.getImageUrl());

                    SearchResponse.ProductDto.UserDto userDto = new SearchResponse.ProductDto.UserDto();
                    userDto.setId(product.getUser().getId());
                    userDto.setUsername(product.getUser().getUsername());
                    dto.setUser(userDto);

                    dto.setHashtags(product.getHashtags().stream()
                            .map(hashtag -> hashtag.getName())
                            .collect(Collectors.toList()));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<SearchResponse.ExerciseDto> convertToExerciseDtos(List<Exercise> exercises) {
        return exercises.stream()
                .map(exercise -> {
                    SearchResponse.ExerciseDto dto = new SearchResponse.ExerciseDto();
                    dto.setId(exercise.getId());
                    dto.setName(exercise.getName());

                    // Convert Hashtag entities to strings
                    dto.setHashtags(exercise.getHashtags().stream()
                            .map(Hashtag::getName)
                            .collect(Collectors.toList()));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<SearchResponse.RecentSearchDto> convertToRecentSearchDtos(List<RecentSearch> recentSearches) {
        return recentSearches.stream()
                .map(search -> {
                    SearchResponse.RecentSearchDto dto = new SearchResponse.RecentSearchDto();
                    dto.setKeyword(search.getKeyword());     // getSearchTerm -> getKeyword
                    dto.setSearchTime(search.getSearchTime()); // getCreatedAt -> getSearchTime
                    return dto;
                })
                .collect(Collectors.toList());
    }
}