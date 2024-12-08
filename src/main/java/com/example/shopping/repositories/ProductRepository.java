package com.example.shopping.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shopping.models.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>{

    public List<Product> findAllByIsActiveTrue();

    public List<Product> findByCategory(String category);

    List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2);

    public Page<Product> findByIsActiveTrue(Pageable pageable);

    public Page<Product> findByCategory(Pageable pageable, String category);

    public Page<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2,
            Pageable pageable);
}
