package com.ardkyer.borrowme;

import com.ardkyer.borrowme.entity.Hashtag;
import com.ardkyer.borrowme.entity.Product;
import com.ardkyer.borrowme.entity.User;
import com.ardkyer.borrowme.repository.ProductRepository;
import com.ardkyer.borrowme.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ProductQueryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        transactionTemplate.execute(status -> {
            entityManager.createQuery("DELETE FROM Product").executeUpdate();
            entityManager.createQuery("DELETE FROM Hashtag").executeUpdate();
            entityManager.createQuery("DELETE FROM User").executeUpdate();
            return null;
        });

        transactionTemplate.execute(status -> {
            Hashtag tag1 = new Hashtag();
            tag1.setName("testTag1");
            entityManager.persist(tag1);

            Hashtag tag2 = new Hashtag();
            tag2.setName("testTag2");
            entityManager.persist(tag2);

            for (int i = 1; i <= 20; i++) {
                User user = new User();
                user.setUsername("queryuser" + i + "@test.com");
                user.setEmail("queryuser" + i + "@test.com");
                user.setPasswordHash("$2a$10$dummyhash");
                user.setEmailVerified(true);
                entityManager.persist(user);

                Product product = new Product();
                product.setTitle("Query Test Product " + i);
                product.setDescription("Desc " + i);
                product.setImageUrl("test" + i + ".jpg");
                product.setTotalQuantity(10);
                product.setAvailableQuantity(10);
                product.setReservationStatus(Product.ReservationStatus.AVAILABLE);
                product.setUser(user);

                Set<Hashtag> tags = new HashSet<>();
                tags.add(tag1);
                if (i % 2 == 0) tags.add(tag2);
                product.setHashtags(tags);

                entityManager.persist(product);
            }
            return null;
        });
    }

    @Test
    @DisplayName("getAllProductsWithDetails - fetch join으로 user/hashtag 즉시 로딩 확인")
    void getAllProductsWithDetails_shouldFetchJoinUserAndHashtags() {
        // OSIV=false 상태에서 트랜잭션 밖 접근 → fetch join 없으면 LazyInitializationException
        List<Product> products = productService.getAllProductsWithDetails();

        assertThat(products).hasSize(20);

        // fetch join 적용 검증: 트랜잭션 종료 후에도 lazy proxy 접근 가능
        for (Product product : products) {
            assertThat(product.getUser()).isNotNull();
            assertThat(product.getUser().getUsername()).isNotBlank();
            assertThat(product.getHashtags()).isNotNull();
        }
    }

    @Test
    @DisplayName("상품 20개 조회 시 전체 데이터 정합성 확인")
    void getAllProductsWithDetails_dataIntegrity() {
        List<Product> products = productService.getAllProductsWithDetails();

        assertThat(products).hasSize(20);

        // 짝수 번째 상품은 해시태그 2개, 홀수는 1개
        long twoTagCount = products.stream()
                .filter(p -> p.getHashtags().size() == 2)
                .count();
        assertThat(twoTagCount).isEqualTo(10);
    }
}
