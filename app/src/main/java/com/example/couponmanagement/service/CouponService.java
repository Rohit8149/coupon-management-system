package com.example.couponmanagement.service;

import com.example.couponmanagement.model.Coupon;
import com.example.couponmanagement.repository.CouponRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @PostConstruct
    public void initSampleData() {
        if (couponRepository.count() == 0) {
            couponRepository.save(new Coupon("WELCOME20", "20% off on first retail order", "PERCENTAGE", 20.0, 50.0, 500, LocalDate.now().plusMonths(3), "ACTIVE"));
            couponRepository.save(new Coupon("FLAT50", "Flat $50 off on orders above $200", "FLAT", 50.0, 200.0, 200, LocalDate.now().plusMonths(1), "ACTIVE"));
            couponRepository.save(new Coupon("FESTIVE10", "Seasonal 10% holiday discount", "PERCENTAGE", 10.0, 30.0, 1000, LocalDate.now().plusMonths(6), "DRAFT"));
            couponRepository.save(new Coupon("EXPIRED99", "Expired clearance promotion", "PERCENTAGE", 30.0, 100.0, 50, LocalDate.now().minusDays(5), "EXPIRED"));
        }
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public Optional<Coupon> getCouponById(Long id) {
        return couponRepository.findById(id);
    }

    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code);
    }

    public Coupon createCoupon(Coupon coupon) {
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Coupon code cannot be empty");
        }
        String cleanCode = coupon.getCode().trim().toUpperCase();
        if (!cleanCode.matches("^[A-Z0-9_-]{3,30}$")) {
            throw new IllegalArgumentException("Coupon code must be between 3 and 30 alphanumeric characters");
        }
        // DELIBERATE DEFECT: Bypassing duplicate code check to trigger Selenium Quality Gate failure in Jenkins
        /*
        if (couponRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new IllegalArgumentException("Coupon code '" + cleanCode + "' already exists! Please choose a unique code.");
        }
        */
        if (coupon.getDiscountValue() == null || coupon.getDiscountValue() <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than zero");
        }
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType()) && coupon.getDiscountValue() > 100.0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100%");
        }
        if (coupon.getMinOrderAmount() == null || coupon.getMinOrderAmount() < 0) {
            throw new IllegalArgumentException("Minimum order amount cannot be negative");
        }
        if (coupon.getUsageLimit() == null || coupon.getUsageLimit() <= 0) {
            throw new IllegalArgumentException("Usage limit must be at least 1 redemption");
        }
        if (coupon.getExpiryDate() == null || coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date must be in the future");
        }
        coupon.setCode(cleanCode);
        if (coupon.getStatus() == null || coupon.getStatus().trim().isEmpty()) {
            coupon.setStatus("DRAFT");
        }
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());
        return couponRepository.save(coupon);
    }

    public List<Coupon> searchCoupons(String query, String status) {
        boolean hasQuery = query != null && !query.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL");

        if (hasQuery && hasStatus) {
            return couponRepository.findByCodeContainingIgnoreCaseAndStatus(query.trim(), status.trim());
        } else if (hasQuery) {
            return couponRepository.findByCodeContainingIgnoreCase(query.trim());
        } else if (hasStatus) {
            return couponRepository.findByStatus(status.trim());
        } else {
            return couponRepository.findAll();
        }
    }

    public Coupon updateStatus(Long id, String targetStatus) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Coupon not found with id: " + id));

        String current = coupon.getStatus();
        String next = targetStatus.toUpperCase().trim();

        // Enforce valid lifecycle: DRAFT -> ACTIVE -> EXPIRED or REDEEMED
        if ("ACTIVE".equals(next) && !"DRAFT".equals(current)) {
            throw new IllegalStateException("Only DRAFT coupons can be activated");
        }
        if ("EXPIRED".equals(next) && (!"ACTIVE".equals(current) && !"DRAFT".equals(current))) {
            throw new IllegalStateException("Only ACTIVE or DRAFT coupons can be expired");
        }
        if ("REDEEMED".equals(next) && !"ACTIVE".equals(current)) {
            throw new IllegalStateException("Only ACTIVE coupons can be redeemed");
        }

        coupon.setStatus(next);
        coupon.setUpdatedAt(LocalDateTime.now());
        return couponRepository.save(coupon);
    }

    public Coupon redeemCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Coupon not found with id: " + id));

        if (!"ACTIVE".equals(coupon.getStatus())) {
            throw new IllegalStateException("Coupon is not ACTIVE and cannot be redeemed");
        }
        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            coupon.setStatus("EXPIRED");
            couponRepository.save(coupon);
            throw new IllegalStateException("Coupon has expired");
        }

        int currentCount = coupon.getRedemptionCount() != null ? coupon.getRedemptionCount() : 0;
        coupon.setRedemptionCount(currentCount + 1);

        if (coupon.getRedemptionCount() >= coupon.getUsageLimit()) {
            coupon.setStatus("REDEEMED");
        }
        coupon.setUpdatedAt(LocalDateTime.now());
        return couponRepository.save(coupon);
    }

    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        long total = couponRepository.count();
        long active = couponRepository.countByStatus("ACTIVE");
        long draft = couponRepository.countByStatus("DRAFT");
        long expired = couponRepository.countByStatus("EXPIRED");
        long redeemed = couponRepository.countByStatus("REDEEMED");

        long totalRedemptions = couponRepository.findAll().stream()
                .mapToInt(c -> c.getRedemptionCount() != null ? c.getRedemptionCount() : 0)
                .sum();

        metrics.put("totalCoupons", total);
        metrics.put("activeCoupons", active);
        metrics.put("draftCoupons", draft);
        metrics.put("expiredCoupons", expired);
        metrics.put("redeemedCoupons", redeemed);
        metrics.put("totalRedemptions", totalRedemptions);
        return metrics;
    }
}
