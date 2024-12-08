package com.example.shopping.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CouponRequest {
    private String code;
    private Double orderAmount;
}
