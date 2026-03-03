package com.ardkyer.borrowme.repository;

import com.ardkyer.borrowme.entity.Comment;
import com.ardkyer.borrowme.entity.CommentLike;
import com.ardkyer.borrowme.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    CommentLike findByCommentAndUser(Comment comment, User user);
}