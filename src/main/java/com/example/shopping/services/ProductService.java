package com.example.shopping.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.example.shopping.models.Product;

public interface ProductService {

    public Product saveProduct(Product product);

    public List<Product> getAllProduct();

    public Boolean deleteProduct(int id);

    public Product getProductByItem(int id);

    public Product updateProduct(Product product, MultipartFile image);

    public List<Product> getAllActiveProduct(String category);

    public List<Product> searchProduct(String ch);

    public Page<Product> getAllActiveProductPage(int pageNo, int pageSize, String category);

    public Page<Product> searchProductPage(int pageNo, int pageSize, String ch);

    public Page<Product> getAllProductPage(int pageNo, int pageSize);

    public List<Product> getProductsByCategory(String category);
}
