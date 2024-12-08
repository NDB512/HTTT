package com.example.shopping.utils;

import java.util.Arrays;

// enum (viết tắt của "enumeration") là một kiểu dữ liệu đặc biệt dùng để định nghĩa một tập hợp các hằng số có tên
public enum OrderEnum {
    IN_PROGRESS(1, "Đang xử lý"),
    PRODUCT_PACKED(2, "Đã đóng gói sản phẩm"),
    OUT_FOR_DELIVERY(3, "Đang giao hàng"),
    DELIVERED(4, "Đã giao hàng"),
    ORDER_RECEIVED(5, "Đã nhận đơn hàng"),
    CANCEL(6,"Hủy");

    private int id;
    private String name;

    private OrderEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static boolean isValidStatus(String status) {
        return Arrays.stream(OrderEnum.values()).anyMatch(e -> e.getName().equals(status));
    }
}
