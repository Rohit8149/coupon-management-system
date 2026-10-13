package com.example.couponmanagement.controller;

import com.example.couponmanagement.model.Coupon;
import com.example.couponmanagement.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponApiController {

    private final CouponService couponService;

    public CouponApiController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    public List<Coupon> getAllCoupons(@RequestParam(value = "query", required = false) String query,
                                      @RequestParam(value = "status", required = false) String status) {
        return couponService.searchCoupons(query, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable Long id) {
        return couponService.getCouponById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCoupon(@Valid @RequestBody Coupon coupon) {
        try {
            Coupon created = couponService.createCoupon(coupon);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String newStatus = payload.get("status");
            Coupon updated = couponService.updateStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/redeem")
    public ResponseEntity<?> redeem(@PathVariable Long id) {
        try {
            Coupon updated = couponService.redeemCoupon(id);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        return couponService.getDashboardMetrics();
    }
}
