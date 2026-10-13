package com.example.couponmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 20, message = "Coupon code must be between 3 and 20 characters")
    @Column(nullable = false, unique = true)
    private String code;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Discount type is required")
    @Column(nullable = false)
    private String discountType; // PERCENTAGE or FLAT

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be greater than zero")
    @Column(nullable = false)
    private Double discountValue;

    @NotNull(message = "Minimum order amount is required")
    @PositiveOrZero(message = "Minimum order amount cannot be negative")
    private Double minOrderAmount;

    @NotNull(message = "Usage limit is required")
    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    private Integer redemptionCount = 0;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String status = "DRAFT"; // DRAFT, ACTIVE, EXPIRED, REDEEMED

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Coupon() {}

    public Coupon(String code, String description, String discountType, Double discountValue, 
                  Double minOrderAmount, Integer usageLimit, LocalDate expiryDate, String status) {
        this.code = code != null ? code.toUpperCase().trim() : null;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.usageLimit = usageLimit;
        this.expiryDate = expiryDate;
        this.status = status != null ? status : "DRAFT";
        this.redemptionCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code != null ? code.toUpperCase().trim() : null; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

    public Double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public Integer getRedemptionCount() { return redemptionCount; }
    public void setRedemptionCount(Integer redemptionCount) { this.redemptionCount = redemptionCount; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
