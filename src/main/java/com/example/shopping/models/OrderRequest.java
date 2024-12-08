package com.example.shopping.models;

import java.util.UUID;

import lombok.Data;

@Data
public class OrderRequest {

    private UUID addressId;

    private String paymentType;

    private Double totalOrderPrice;     // Tổng giá trị đơn hàng
    private Double deliveryPrice;       // Phí vận chuyển
    private Double discount;
    
}
