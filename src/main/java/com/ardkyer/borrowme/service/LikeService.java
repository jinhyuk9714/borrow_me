package com.ardkyer.borrowme.service;

import com.ardkyer.borrowme.entity.*;

public interface LikeService {
    boolean toggleLike(User user, Product product);
    void removeLike(User user, Product product);
    boolean hasUserLikedProduct(User user, Product product);
    long getLikeCount(Product product);
}
