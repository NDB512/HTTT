package com.example.shopping.services.impls;

import java.util.List;
import java.io.File;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.shopping.models.Category;
import com.example.shopping.repositories.CategoryRepository;
import com.example.shopping.services.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public boolean existCategory(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Boolean deleteCategory(int id) {
        Category category = categoryRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(category)) {
            // Đường dẫn đến hình ảnh danh mục
            String imagePath = "src/main/resources/static/img/category_img/" + category.getImageName(); // Cập nhật đường dẫn nếu cần
            
            // Kiểm tra xem hình ảnh có phải là hình ảnh mặc định không
            if (!"default.jpg".equals(category.getImageName())) {
                File imageFile = new File(imagePath);
                try {
                    if (imageFile.exists()) {
                        imageFile.delete(); // Xóa hình ảnh
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Xóa danh mục
            categoryRepository.delete(category);
            return true;
        }

        return false;
    }

    @Override
    public Category getCategoryById(int id) {
        Category category = categoryRepository.findById(id).orElse(null);

        return category;
    }

    @Override
    public List<Category> getAllActiveCategory() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();

        return categories;
    }

    @Override
    public Page<Category> getAllCategoryPage(int pageNo, int pageSize) {
        Pageable pageable= PageRequest.of(pageNo, pageSize);
        return categoryRepository.findAll(pageable);
    }
    
}   
