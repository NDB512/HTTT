package com.example.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SalesReportDto {
    private String date; // Ngày (hoặc tháng, năm) của báo cáo
    private Double totalRevenue; // Tổng doanh thu
    private int totalOrders; // Tổng số đơn hàng
}