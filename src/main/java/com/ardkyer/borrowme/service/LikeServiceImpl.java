package com.ardkyer.borrowme.service;

import com.ardkyer.borrowme.entity.*;
import com.ardkyer.borrowme.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;

    @Autowired
    public LikeServiceImpl(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Override
    @Transactional
    public boolean toggleLike(User user, Product product) {
        Optional<Like> existingLike = likeRepository.findByUserAndProduct(user, product);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return false;
        } else {
            Like newLike = new Like();
            newLike.setUser(user);
            newLike.setProduct(product);
            likeRepository.save(newLike);
            return true;
        }
    }

    @Override
    @Transactional
    public void removeLike(User user, Product product) {
        likeRepository.deleteByUserAndProduct(user, product);
    }

    @Override
    public boolean hasUserLikedProduct(User user, Product product) {
        return likeRepository.existsByUserAndProduct(user, product);
    }

    @Override
    public long getLikeCount(Product product) {
        return likeRepository.countByProduct(product);
    }
}
