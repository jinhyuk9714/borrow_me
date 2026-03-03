package com.ardkyer.borrowme.repository;

import com.ardkyer.borrowme.entity.Like;
import com.ardkyer.borrowme.entity.User;
import com.ardkyer.borrowme.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndProduct(User user, Product product);
    void deleteByUserAndProduct(User user, Product product);
    boolean existsByUserAndProduct(User user, Product product);
    long countByProduct(Product product);
}
