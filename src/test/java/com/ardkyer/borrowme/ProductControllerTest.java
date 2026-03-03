package com.ardkyer.borrowme;

import com.ardkyer.borrowme.config.SecurityConfig;
import com.ardkyer.borrowme.controller.ProductController;
import com.ardkyer.borrowme.entity.Product;
import com.ardkyer.borrowme.entity.User;
import com.ardkyer.borrowme.entity.Reservation;
import com.ardkyer.borrowme.security.JwtAuthenticationFilter;
import com.ardkyer.borrowme.security.JwtTokenProvider;
import com.ardkyer.borrowme.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
        )
)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private UserService userService;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private FollowService followService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private Product createTestProduct(User owner) {
        Product product = new Product();
        product.setId(1L);
        product.setTitle("Test Product");
        product.setDescription("Test Description");
        product.setImageUrl("test.jpg");
        product.setTotalQuantity(10);
        product.setAvailableQuantity(10);
        product.setReservationStatus(Product.ReservationStatus.AVAILABLE);
        product.setUser(owner);
        product.setHashtags(new HashSet<>());
        return product;
    }

    private User createTestUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username);
        return user;
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("자기 상품 예약 시도 → 400 Bad Request")
    void reserveOwnProduct_shouldReturn400() throws Exception {
        User owner = createTestUser(1L, "user@test.com");
        Product product = createTestProduct(owner);

        given(userService.findByUsername("user@test.com")).willReturn(owner);
        given(productService.getProductById(1L)).willReturn(Optional.of(product));

        mockMvc.perform(post("/api/products/1/reserve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "buyer@test.com")
    @DisplayName("유효하지 않은 수량 예약 → 400 Bad Request")
    void reserveWithInvalidQuantity_shouldReturn400() throws Exception {
        User owner = createTestUser(1L, "owner@test.com");
        User buyer = createTestUser(2L, "buyer@test.com");
        Product product = createTestProduct(owner);

        given(userService.findByUsername("buyer@test.com")).willReturn(buyer);
        given(productService.getProductById(1L)).willReturn(Optional.of(product));

        mockMvc.perform(post("/api/products/1/reserve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "buyer@test.com")
    @DisplayName("정상 예약 → 200 OK + reservationId 반환")
    void reserveProduct_shouldReturn200() throws Exception {
        User owner = createTestUser(1L, "owner@test.com");
        User buyer = createTestUser(2L, "buyer@test.com");
        Product product = createTestProduct(owner);

        Product updatedProduct = createTestProduct(owner);
        updatedProduct.setAvailableQuantity(9);

        Reservation reservation = new Reservation();
        reservation.setId(100L);
        reservation.setProduct(updatedProduct);
        reservation.setUser(buyer);
        reservation.setQuantity(1);

        given(userService.findByUsername("buyer@test.com")).willReturn(buyer);
        given(productService.getProductById(1L)).willReturn(Optional.of(product));
        given(reservationService.reserve(any(Product.class), eq(buyer), eq(1))).willReturn(reservation);

        mockMvc.perform(post("/api/products/1/reserve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(100))
                .andExpect(jsonPath("$.remainingQuantity").value(9));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("존재하지 않는 상품 예약 → 400 Bad Request")
    void reserveNonExistentProduct_shouldReturn400() throws Exception {
        User user = createTestUser(1L, "user@test.com");
        given(userService.findByUsername("user@test.com")).willReturn(user);
        given(productService.getProductById(999L)).willReturn(Optional.empty());

        mockMvc.perform(post("/api/products/999/reserve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("상품 목록 조회 → 200 OK")
    void getProducts_shouldReturn200() throws Exception {
        given(userService.findByUsername("user@test.com")).willReturn(createTestUser(1L, "user@test.com"));
        given(productService.getAllProductsWithDetails()).willReturn(java.util.List.of());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }
}
