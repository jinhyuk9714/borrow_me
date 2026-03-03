package com.ardkyer.borrowme.service;

import com.ardkyer.borrowme.entity.*;
import com.ardkyer.borrowme.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              NotificationRepository notificationRepository) {
        this.commentRepository = commentRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public Comment addComment(Comment comment) {
        Comment savedComment = commentRepository.save(comment);

        if (savedComment.getProduct() != null) {
            log.debug("상품 ID: {}", savedComment.getProduct().getId());
        } else {
            log.debug("상품이 null입니다.");
        }

        // 자신의 게시물이 아닐 경우에만 알림 생성
        if (!savedComment.getProduct().getUser().equals(savedComment.getUser())) {
            Notification notification = new Notification();
            notification.setUser(savedComment.getProduct().getUser());
            notification.setType(NotificationType.COMMENT);  // 알림 타입 설정
            notification.setProduct(savedComment.getProduct());
            notification.setComment(savedComment);  // comment 엔티티 설정
            notification.setPostTitle(savedComment.getProduct().getTitle());
            notification.setCommenterName(savedComment.getUser().getUsername());
            notification.setCommentContent(savedComment.getContent());
            notification.setMessage(String.format("[%s] %s님이 댓글을 달았습니다: %s",
                    savedComment.getProduct().getTitle(),
                    savedComment.getUser().getUsername(),
                    savedComment.getContent()));

            notificationRepository.save(notification);
        }

        return savedComment;
    }

    @Override
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    public Page<Comment> getCommentsByProduct(Product product, Pageable pageable) {
        return commentRepository.findByProductOrderByCreatedAtDesc(product, pageable);
    }

    @Override
    @Transactional
    public Comment updateComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    @Transactional  // 메서드 레벨에도 @Transactional 추가
    public void deleteComment(Long id) {
        try {
            // 1. 해당 댓글과 관련된 알림 삭제
            notificationRepository.deleteAllByParentCommentId(id);  // 답글 관련 알림 먼저 삭제
            notificationRepository.deleteAllByCommentId(id);        // 댓글 관련 알림 삭제

            // 2. 댓글 삭제 (cascade로 인해 답글도 자동 삭제됨)
            commentRepository.deleteById(id);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete comment: " + e.getMessage());
        }
    }

    @Override
    public long getCommentCount(Product product) {
        return commentRepository.countByProduct(product);
    }

    @Override
    public Optional<Comment> getRecentComment(Product product) {
        return commentRepository.findFirstByProductOrderByCreatedAtDesc(product);
    }

    @Override
    public List<Comment> getCommentsByUser(User user) {
        return commentRepository.findByUser(user);
    }
}