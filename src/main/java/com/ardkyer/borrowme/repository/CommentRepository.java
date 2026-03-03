package com.ardkyer.borrowme.repository;

import com.ardkyer.borrowme.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByProductOrderByCreatedAtDesc(Product product, Pageable pageable);
    long countByProduct(Product product);
    List<Comment> findByUser(User user);
    Optional<Comment> findFirstByProductOrderByCreatedAtDesc(Product product);
}
