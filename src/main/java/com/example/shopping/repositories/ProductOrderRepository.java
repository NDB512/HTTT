package com.example.shopping.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shopping.models.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer>{

    @EntityGraph(attributePaths = "items")
    List<ProductOrder> findByUserId(int userId);

    public ProductOrder findByOrderId(String orderId);

    @Query("SELECT po FROM ProductOrder po WHERE po.orderedDay BETWEEN :startDate AND :endDate")
    List<ProductOrder> findOrdersWithinDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT po FROM ProductOrder po WHERE po.orderedDay BETWEEN :startDate AND :endDate AND po.status = :status")
    List<ProductOrder> findOrdersWithinDateRangeAndStatus(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate,
                                                       @Param("status") String status);

}
