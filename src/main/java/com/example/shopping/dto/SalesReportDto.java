package com.example.shopping.dto;

import java.util.List;
import java.util.Map;

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
    private List<Map.Entry<String, Integer>> topProducts; // Top 3 sản phẩm
    private List<Map.Entry<String, Integer>> topUsers; // Top 3 người dùng
}