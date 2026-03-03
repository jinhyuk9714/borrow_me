package com.ardkyer.borrowme.repository;

import com.ardkyer.borrowme.entity.Reservation;
import com.ardkyer.borrowme.entity.User;
import com.ardkyer.borrowme.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByProductAndUser(Product product, User user);
    List<Reservation> findByProduct(Product product);
    List<Reservation> findByUser(User user);
}
