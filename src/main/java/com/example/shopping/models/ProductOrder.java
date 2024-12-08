package com.example.shopping.models;

import java.time.LocalDateTime;
import java.util.*;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String orderId;

    private LocalDateTime orderedDay;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserDtls user;

    private String status;

    private String paymentType;

    @ManyToOne // Quan hệ với Address
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address; // Lưu trực tiếp Address

    private Double deliveryPrice;       // Phí vận chuyển
    private Double discount;
    private Double totalOrderPrice;     // Tổng giá trị đơn hàng
    
    private LocalDateTime orderDate = LocalDateTime.now();
    private LocalDateTime deliverDate = orderDate.plusDays(7);
}