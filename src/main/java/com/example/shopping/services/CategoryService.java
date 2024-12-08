package com.example.shopping.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.shopping.models.Category;

public interface CategoryService {

    public Category saveCategory(Category category);

    public boolean existCategory(String name);

    public List<Category> getAllCategories();

    public Boolean deleteCategory(int id);

    public Category getCategoryById(int id);

    public List<Category> getAllActiveCategory();

    public Page<Category> getAllCategoryPage(int pageNo, int pageSize);
}