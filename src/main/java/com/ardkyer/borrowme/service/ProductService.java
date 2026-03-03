package com.ardkyer.borrowme.service;

import com.amazonaws.services.s3.model.S3Object;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ardkyer.borrowme.entity.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public interface ProductService {
    Product uploadProduct(Product product, MultipartFile file, Set<String> hashtagNames) throws IOException;
    Optional<Product> getProductById(Long id);
    Product updateProduct(Product product);
    void deleteProduct(Long id);

    S3Object getProductFile(String fileName);

    List<Product> getAllProducts();
    List<Product> getAllProductsWithDetails();
    List<Product> getProductsByUser(User user);
    List<Product> getRecentProductsByUser(User user, int limit);
    List<Product> getRecentProductsByUsers(List<User> users, int limit);
    List<Product> getRandomRecentProducts(int count);

    List<Product> getReservedProductsByUser(User user);
    boolean isAvailableForReservation(Long productId, int quantity);
    Product updateAvailableQuantity(Long productId, int quantity);

    List<Product> searchProducts(String query);
    List<Product> searchProductsByHashtags(Set<String> hashtags);
    void saveHashtagsFromDescription(String description);

    List<Product> getAllProductsWithComments();
    List<Product> getAllProductsWithSortedComments();

    Product updateProductWithImage(Product product, MultipartFile file) throws IOException;
}
