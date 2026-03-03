
package com.ardkyer.borrowme.service;

import com.ardkyer.borrowme.entity.Product;
import com.ardkyer.borrowme.entity.User;
import com.ardkyer.borrowme.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReservationService {
    // 예약 생성
    Reservation reserve(Product product, User user, int quantity);

    // 예약 취소
    void cancelReservation(Long reservationId);

    // 사용자별 예약 조회
    List<Reservation> getUserReservations(User user);

    // 상품별 예약 조회
    List<Reservation> getProductReservations(Product product);

    // 특정 사용자의 특정 상품 예약 조회
    List<Reservation> getReservationByProductAndUser(Product product, User user);

    // 예약 상태 변경
    Reservation updateReservationStatus(Long reservationId, Reservation.ReservationStatus status);

    // 예약 가능 여부 확인
    boolean canReserve(Product product, int quantity);
}