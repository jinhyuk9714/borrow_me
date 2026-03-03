//package com.ardkyer.borrowme.controller;
//
//import com.ardkyer.borrowme.dto.request.LoginRequest;
//import com.ardkyer.borrowme.dto.request.SignupRequest;
//import com.ardkyer.borrowme.dto.response.LoginResponse;
//import com.ardkyer.borrowme.dto.response.NotificationCountResponse;
//import com.ardkyer.borrowme.dto.response.UserResponse;
//import com.ardkyer.borrowme.entity.User;
//import com.ardkyer.borrowme.repository.NotificationRepository;
//import com.ardkyer.borrowme.security.PrincipalDetails;
//import com.ardkyer.borrowme.service.UserService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//@RestController
//@RequestMapping("/api")
//@Tag(name = "Web", description = "REST API endpoints")
//@RequiredArgsConstructor
//public class WebController {
//
//    private final UserService userService;
//    private final NotificationRepository notificationRepository;
//
//    @PostMapping("/auth/login")
//    @Operation(summary = "Login user")
//    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
//        try {
//            LoginResponse response = userService.login(request);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PostMapping("/auth/register")
//    @Operation(summary = "Register new user")
//    public ResponseEntity<?> register(@RequestBody SignupRequest request) {
//        try {
//            UserResponse response = userService.registerUser(request);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @GetMapping("/notifications/unread-count")
//    @Operation(summary = "Get unread notifications count")
//    public ResponseEntity<?> getUnreadNotificationsCount(
//            @AuthenticationPrincipal PrincipalDetails principalDetails) {
//        int unreadCount = notificationRepository.countByUserAndIsReadFalse(
//                principalDetails.getUser()
//        );
//        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
//    }
//}