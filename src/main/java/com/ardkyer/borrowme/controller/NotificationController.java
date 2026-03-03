package com.ardkyer.borrowme.controller;

import com.ardkyer.borrowme.entity.Notification;
import com.ardkyer.borrowme.security.PrincipalDetails;
import com.ardkyer.borrowme.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<Notification> notifications = notificationService.getNotificationsForUser(principalDetails.getUser());

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<Map<String, Object>> readNotification(@PathVariable Long id,
                                                 @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Notification notification = notificationService.getNotification(id);

        if (!notification.getUser().getId().equals(principalDetails.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        notificationService.markAsRead(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("redirectUrl", notification.getProduct() != null
                ? "/products/detail/" + notification.getProduct().getId()
                : "/notifications");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-read")
    public ResponseEntity<Map<String, Object>> deleteReadNotifications(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        notificationService.deleteReadNotifications(principalDetails.getUser());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "읽은 알림이 삭제되었습니다."
        ));
    }
}
