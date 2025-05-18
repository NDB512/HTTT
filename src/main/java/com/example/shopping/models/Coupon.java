package com.example.shopping.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private Double discountAmount; // Giá trị giảm giá
    private Double discountPercentage; // Phần trăm giảm giá

    private LocalDateTime startDate; // Ngày bắt đầu hiệu lực
    private LocalDateTime endDate;   // Ngày kết thúc hiệu lực

    private Integer usageLimit; // Số lần sử dụng tối đa

    @Enumerated(EnumType.STRING)
    private CouponStatus status; // ACTIVE, EXPIRED, USED

    // Điều kiện áp dụng
    private Double minimumOrderAmount; // Giá trị đơn hàng tối thiểu để áp dụng coupon
}
