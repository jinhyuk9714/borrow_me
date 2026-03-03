package com.ardkyer.borrowme.controller;

import com.ardkyer.borrowme.dto.*;
import com.ardkyer.borrowme.entity.*;
import com.ardkyer.borrowme.repository.NotificationRepository;
import com.ardkyer.borrowme.security.PrincipalDetails;
import com.ardkyer.borrowme.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.ardkyer.borrowme.dto.request.LoginRequest;
import com.ardkyer.borrowme.dto.request.SignupRequest;
import com.ardkyer.borrowme.dto.response.LoginResponse;
import com.ardkyer.borrowme.dto.response.UserResponse;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "User", description = "User management API")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final NotificationRepository notificationRepository;

    @PostMapping("/auth/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/notifications/unread-count")
    @Operation(summary = "Get unread notifications count")
    public ResponseEntity<?> getUnreadNotificationsCount(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        int unreadCount = notificationRepository.countByUserAndIsReadFalse(
                principalDetails.getUser()
        );
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    // 기존의 다른 API 엔드포인트들...
}