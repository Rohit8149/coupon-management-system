package com.example.couponmanagement.repository;

import com.example.couponmanagement.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<Coupon> findByStatus(String status);
    List<Coupon> findByCodeContainingIgnoreCase(String query);
    List<Coupon> findByCodeContainingIgnoreCaseAndStatus(String code, String status);
    long countByStatus(String status);
}
