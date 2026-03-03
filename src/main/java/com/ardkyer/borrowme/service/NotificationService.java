package com.ardkyer.borrowme.service;

import com.ardkyer.borrowme.entity.*;
import com.ardkyer.borrowme.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createReplyNotification(User recipient, Reply reply) {
        Comment parentComment = reply.getParentComment();
        Product product = parentComment.getProduct();

        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setType(NotificationType.REPLY);
        notification.setProduct(product);
        notification.setComment(parentComment);
        notification.setReply(reply);
        notification.setPostTitle(product.getTitle());
        notification.setCommenterName(reply.getUser().getUsername());
        notification.setCommentContent(reply.getContent());
        notification.setMessage(String.format("[%s] %s님이 회원님의 댓글에 답글을 달았습니다: %s",
                product.getTitle(),
                reply.getUser().getUsername(),
                reply.getContent()));

        return notificationRepository.save(notification);
    }

    // 다른 종류의 알림을 위한 메소드들...
    public Notification createCommentNotification(User recipient, Comment comment) {
        Product product = comment.getProduct();

        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setType(NotificationType.COMMENT);
        notification.setProduct(product);
        notification.setComment(comment);
        notification.setPostTitle(product.getTitle());
        notification.setCommenterName(comment.getUser().getUsername());
        notification.setCommentContent(comment.getContent());
        notification.setMessage(String.format("[%s] %s님이 댓글을 달았습니다: %s",
                product.getTitle(),
                comment.getUser().getUsername(),
                comment.getContent()));

        return notificationRepository.save(notification);
    }

    public Notification getNotification(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
    }

    public void markAsRead(Long id) {
        Notification notification = getNotification(id);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void deleteReadNotifications(User user) {
        List<Notification> readNotifications = notificationRepository.findByUserAndIsReadTrue(user);
        notificationRepository.deleteAll(readNotifications);
    }
}