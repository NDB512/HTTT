package com.example.shopping.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shopping.models.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    
}
