package com.example.shopping.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 80)
    private String title;

    @Column(length = 1000)
    private String description;

    private String category; // Tên danh mục hoặc ID danh mục

    private double price; // Giá gốc

    private int stock; // Số lượng tồn kho

    private String image; // Tên file hình ảnh sản phẩm

    private LocalDateTime createdDate; // Ngày tạo sản phẩm

    private LocalDateTime updateDate;

    private Double discount; // Giảm giá (theo phần trăm)

    private Boolean isActive; // Trạng thái sản phẩm (Kích hoạt hay không)

    private double productTaxRate; // Thuế sản phẩm (tính thêm vào)

    private double profitMargin; // Lợi nhuận mong muốn (theo phần trăm)

    private Double finalPrice; // Giá bán cuối cùng (sau thuế và lợi nhuận)

    private String sizes;

    private Boolean isNew; 

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

}
