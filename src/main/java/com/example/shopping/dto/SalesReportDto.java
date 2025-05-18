package com.example.shopping.dto;

import java.util.List;
import java.util.Map;

public class SalesReportDto {
    private String period; // Có thể là tháng (YYYY-MM) hoặc ngày (YYYY-MM-DD)
    private Double totalRevenue;
    private Integer totalOrders;
    private List<Map.Entry<String, Integer>> topProducts;
    private List<Map.Entry<String, Integer>> topUsers;

    public SalesReportDto(String period, Double totalRevenue, Integer totalOrders, 
                         List<Map.Entry<String, Integer>> topProducts, 
                         List<Map.Entry<String, Integer>> topUsers) {
        this.period = period;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.topProducts = topProducts;
        this.topUsers = topUsers;
    }

    // Getters and Setters
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public List<Map.Entry<String, Integer>> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<Map.Entry<String, Integer>> topProducts) {
        this.topProducts = topProducts;
    }

    public List<Map.Entry<String, Integer>> getTopUsers() {
        return topUsers;
    }

    public void setTopUsers(List<Map.Entry<String, Integer>> topUsers) {
        this.topUsers = topUsers;
    }
}