package com.example.shopping.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shopping.models.WarehouseReceiptForm;

public interface WarehouseReceiptRepository extends JpaRepository<WarehouseReceiptForm, Integer>{
    Page<WarehouseReceiptForm> findByWarehouseReceiptId(String ch, String ch2,
            Pageable pageable);
}
