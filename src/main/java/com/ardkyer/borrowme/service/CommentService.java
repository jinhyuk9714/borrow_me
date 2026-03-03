package com.ardkyer.borrowme.service;

import com.ardkyer.borrowme.entity.Comment;
import com.ardkyer.borrowme.entity.User;
import com.ardkyer.borrowme.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    Comment addComment(Comment comment);
    Optional<Comment> getCommentById(Long id);
    Page<Comment> getCommentsByProduct(Product product, Pageable pageable);
    Comment updateComment(Comment comment);
    void deleteComment(Long id);
    long getCommentCount(Product product);
    Optional<Comment> getRecentComment(Product product);
    List<Comment> getCommentsByUser(User user);
}
