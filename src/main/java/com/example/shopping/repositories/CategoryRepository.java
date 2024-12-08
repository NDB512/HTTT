package com.example.shopping.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.example.shopping.models.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer>{

    public boolean existsByName(String name);

    public List<Category> findByIsActiveTrue();

    public Category findByName(String categoryName);
}
