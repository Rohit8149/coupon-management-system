package com.example.couponmanagement;

import com.example.couponmanagement.model.Coupon;
import com.example.couponmanagement.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouponManagementApplicationTests {

    @Autowired
    private CouponService couponService;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        assertNotNull(couponService, "CouponService bean should be loaded");
    }

    @Test
    @DisplayName("Feature 1: Successfully create a valid coupon")
    void testCreateValidCoupon() {
        Coupon coupon = new Coupon("AUTOTEST10", "Automated test coupon", "PERCENTAGE", 10.0, 50.0, 100, LocalDate.now().plusMonths(2), "DRAFT");
        Coupon created = couponService.createCoupon(coupon);

        assertNotNull(created.getId(), "Persisted coupon should have generated ID");
        assertEquals("AUTOTEST10", created.getCode());
        assertEquals("DRAFT", created.getStatus());
        assertEquals(10.0, created.getDiscountValue());
    }

    @Test
    @DisplayName("Feature 1 Validation Gate: Duplicate code rejection")
    void testRejectDuplicateCouponCode() {
        // WELCOME20 is already seeded in sample data
        Coupon duplicate = new Coupon("WELCOME20", "Duplicate welcome coupon", "PERCENTAGE", 25.0, 40.0, 50, LocalDate.now().plusMonths(1), "DRAFT");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            couponService.createCoupon(duplicate);
        });
        assertTrue(ex.getMessage().contains("already exists"), "Exception message should indicate duplicate code");
    }

    @Test
    @DisplayName("Feature 1 Validation Gate: Reject discount exceeding 100%")
    void testRejectPercentageExceeding100() {
        Coupon invalidPercent = new Coupon("SUPER999", "Invalid 150% discount", "PERCENTAGE", 150.0, 20.0, 100, LocalDate.now().plusMonths(1), "DRAFT");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            couponService.createCoupon(invalidPercent);
        });
        assertTrue(ex.getMessage().contains("cannot exceed 100%"), "Exception message should mention 100% ceiling");
    }

    @Test
    @DisplayName("Feature 1 Validation Gate: Reject past expiry date")
    void testRejectPastExpiryDate() {
        Coupon expiredDate = new Coupon("PASTEXP", "Invalid past coupon", "FLAT", 15.0, 30.0, 50, LocalDate.now().minusDays(2), "DRAFT");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            couponService.createCoupon(expiredDate);
        });
        assertTrue(ex.getMessage().contains("must be in the future"), "Exception message should mention future date");
    }
}
