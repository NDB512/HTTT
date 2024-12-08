package com.example.shopping.dto;


import java.time.LocalDateTime;

import com.example.shopping.models.CouponStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CouponDto {
    private Long id;

    private String code;

    private Double discountAmount; // Giá trị giảm giá
    private Double discountPercentage; // Phần trăm giảm giá

    private LocalDateTime startDate; // Ngày bắt đầu hiệu lực
    private LocalDateTime endDate;   // Ngày kết thúc hiệu lực

    private Integer usageLimit; // Số lần sử dụng tối đa
    private CouponStatus status; // ACTIVE, EXPIRED, USED

    // Điều kiện áp dụng
    private Double minimumOrderAmount; // Giá trị đơn hàng tối thiểu để áp dụng coupon
}
