package com.example.shopping.services.impls;

import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.shopping.models.Product;
import com.example.shopping.repositories.ProductRepository;
import com.example.shopping.services.ProductService;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {

        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProduct(){
        return productRepository.findAll();
    }    

    @Override
    public Page<Product> getAllProductPage(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findAll(pageable);
    }

    @Override
    public Boolean deleteProduct(int id) {
        Product product = productRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(product)) {
            // Kiểm tra hình ảnh có phải là default.jpg không
            String imagePath = "src/main/resources/static/img/product_img/" + product.getImage();
            if (!"default.jpg".equals(product.getImage())) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    imageFile.delete(); // Xóa hình ảnh
                }
            }
            productRepository.delete(product);
            return true;
        }

        return false;
    }

    @Override
    public Product getProductByItem(int id) {
        Product product = productRepository.findById(id).orElse(null);
        return product;
    }

    @Override
    public Product updateProduct(Product product, MultipartFile image) {
        Product dbProduct = getProductByItem(product.getId());
    
        // Kiểm tra xem có ảnh mới được tải lên không
        boolean isImageUpdated = !image.isEmpty();
        String newImageName = isImageUpdated ? image.getOriginalFilename() : dbProduct.getImage();
        String oldImageName = dbProduct.getImage();
    
        // Cập nhật thông tin sản phẩm
        dbProduct.setImage(newImageName);
        dbProduct.setTitle(product.getTitle());
        dbProduct.setDescription(product.getDescription());
        dbProduct.setCategory(product.getCategory());
        dbProduct.setPrice(product.getPrice());
        dbProduct.setStock(product.getStock());
        dbProduct.setDiscount(product.getDiscount());
        dbProduct.setIsActive(product.getIsActive());
        dbProduct.setProfitMargin(product.getProfitMargin());  // Thêm phần trăm lợi nhuận
        dbProduct.setProductTaxRate(product.getProductTaxRate()); // Thêm thuế sản phẩm
    
        // Lấy các giá trị cần thiết
        Double originalPrice = product.getPrice();  // Giá gốc
        Double profitMargin = product.getProfitMargin();  // Lợi nhuận mong muốn (%)
        Double productTaxRate = product.getProductTaxRate(); // Thuế sản phẩm (%)

        // 1. Tính thuế (bao gồm thuế danh mục và thuế sản phẩm)
        Double taxAmount = originalPrice * (productTaxRate) / 100;

        // 2. Tính lợi nhuận mong muốn
        Double profitAmount = originalPrice * profitMargin / 100;

        Double finalPrice = originalPrice + taxAmount + profitAmount;

        // Cập nhật giá niêm yết
        dbProduct.setFinalPrice(finalPrice);
    
        // Lưu sản phẩm
        Product updatedProduct = productRepository.save(dbProduct);
    
        if (updatedProduct != null && isImageUpdated) {
            try {
                // Xóa hình ảnh cũ nếu có và khác với ảnh mặc định
                if (oldImageName != null && !oldImageName.equals("default.jpg")) {
                    String uploadDir = "src/main/resources/static/img/product_img";
                    Path oldFilePath = Path.of(uploadDir, oldImageName);
                    Files.deleteIfExists(oldFilePath); // Xóa tệp cũ nếu tồn tại
                }
    
                // Lưu tệp hình ảnh mới vào thư mục tĩnh
                String uploadDir = "src/main/resources/static/img/product_img";
                Path filePath = Path.of(uploadDir, newImageName);
                image.transferTo(filePath);
            } catch (IOException e) {
                e.printStackTrace(); // Xử lý lỗi khi lưu tệp
            }
        }
    
        return updatedProduct;
    }

    @Override
    public List<Product> getAllActiveProduct(String category) {
        List<Product> products = null;
        if(ObjectUtils.isEmpty(category)){
            products = productRepository.findAllByIsActiveTrue();
        }else{
            products = productRepository.findByCategory(category);
        }
        return products;
    }

    @Override
    public List<Product> searchProduct(String ch) {
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,ch);
    }

    @Override
    public Page<Product> searchProductPage(int pageNo, int pageSize ,String ch) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,ch,pageable);
    }

    @Override
    public Page<Product> getAllActiveProductPage(int pageNo, int pageSize, String category) {
        Pageable pageable= PageRequest.of(pageNo, pageSize);

        Page<Product> pageProduct = null;

        if(ObjectUtils.isEmpty(category)){
            pageProduct = productRepository.findByIsActiveTrue(pageable);
        }else{
            pageProduct = productRepository.findByCategory(pageable, category);
        }
        return pageProduct;
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return productRepository.findAll(); // Nếu không có danh mục, trả về tất cả sản phẩm
        }
        return productRepository.findByCategory(category); // Tìm sản phẩm theo danh mục
    }    
}
