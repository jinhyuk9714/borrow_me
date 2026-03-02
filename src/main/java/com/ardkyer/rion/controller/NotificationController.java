package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.Notification;
import com.ardkyer.rion.security.PrincipalDetails;
import com.ardkyer.rion.service.NotificationService;
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
        Map<String, Object> response = new HashMap<>();
        try {
            Notification notification = notificationService.getNotification(id);

            if (!notification.getUser().getId().equals(principalDetails.getUser().getId())) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            notificationService.markAsRead(id);

            response.put("success", true);
            response.put("redirectUrl", notification.getVideo() != null
                    ? "/videos/detail/" + notification.getVideo().getId()
                    : "/notifications");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error reading notification: ", e);
            response.put("success", false);
            response.put("message", "Error reading notification");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete-read")
    public ResponseEntity<Map<String, Object>> deleteReadNotifications(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            notificationService.deleteReadNotifications(principalDetails.getUser());
            response.put("success", true);
            response.put("message", "읽은 알림이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting read notifications: ", e);
            response.put("success", false);
            response.put("message", "Error deleting read notifications");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
