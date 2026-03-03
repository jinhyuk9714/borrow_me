package com.ardkyer.borrowme.service;

import com.ardkyer.borrowme.entity.Product;
import com.ardkyer.borrowme.entity.User;
import com.ardkyer.borrowme.entity.Reservation;
import com.ardkyer.borrowme.repository.ReservationRepository;
import com.ardkyer.borrowme.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Reservation reserve(Product product, User user, int quantity) {
        // L1 캐시에서 기존 Product 엔티티를 제거하여 SELECT FOR UPDATE가 실제 DB 조회하도록 보장
        entityManager.detach(product);

        // Pessimistic Lock으로 Product를 재조회하여 동시성 보장
        Product lockedProduct = productRepository.findByIdForUpdate(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (lockedProduct.getAvailableQuantity() < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }

        // 새 예약 생성
        Reservation reservation = new Reservation();
        reservation.setProduct(lockedProduct);
        reservation.setUser(user);
        reservation.setQuantity(quantity);
        reservation.setStatus(Reservation.ReservationStatus.PENDING);

        // 재고 수량 감소
        lockedProduct.setAvailableQuantity(lockedProduct.getAvailableQuantity() - quantity);
        if (lockedProduct.getAvailableQuantity() == 0) {
            lockedProduct.setReservationStatus(Product.ReservationStatus.OUT_OF_STOCK);
        } else {
            lockedProduct.setReservationStatus(Product.ReservationStatus.RESERVED);
        }

        productRepository.save(lockedProduct);
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 예약 취소 시 재고 수량 증가
        Product product = reservation.getProduct();
        product.setAvailableQuantity(product.getAvailableQuantity() + reservation.getQuantity());

        // 재고 상태 업데이트
        if (product.getAvailableQuantity() > 0) {
            product.setReservationStatus(Product.ReservationStatus.AVAILABLE);
        }

        productRepository.save(product);
        reservation.setStatus(Reservation.ReservationStatus.CANCELED);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(User user) {
        return reservationRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getProductReservations(Product product) {
        return reservationRepository.findByProduct(product);
    }

    @Override
    public List<Reservation> getReservationByProductAndUser(Product product, User user) {
        return reservationRepository.findByProductAndUser(product, user);
    }

    @Override
    @Transactional
    public Reservation updateReservationStatus(Long reservationId, Reservation.ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        reservation.setStatus(status);
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReserve(Product product, int quantity) {
        return product.getAvailableQuantity() >= quantity &&
                product.getReservationStatus() != Product.ReservationStatus.OUT_OF_STOCK;
    }
}