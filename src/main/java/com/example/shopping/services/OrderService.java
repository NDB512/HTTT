package com.example.shopping.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.example.shopping.dto.SalesReportDto;
import com.example.shopping.models.OrderRequest;
import com.example.shopping.models.ProductOrder;

public interface OrderService {

    public void saveOrder(int userId, OrderRequest orderRequest);

    public List<ProductOrder> getOrdersByUser(int id);

    public ProductOrder updateOrderStatus(int id, String st); 

    public ProductOrder getOrderById(int orderId);

    public List<ProductOrder> getAllOrders();

    public ProductOrder getOrderByOrderid(String orderId);

    public Page<ProductOrder> getAllOrdersPage(int pageNo, int pageSize);

    public void handleStockAfterCancellation(ProductOrder order);

    public List<SalesReportDto> generateSalesReport(LocalDateTime startDate, LocalDateTime endDate);

    public List<SalesReportDto> generateDailySalesReport(String month);

    public long countPendingOrders();
}
