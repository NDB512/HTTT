// package com.example.shopping.models;

// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import lombok.*;


// @AllArgsConstructor
// @NoArgsConstructor
// @Setter
// @Getter
// @Entity
// public class CategoryType {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private int id;

//     private String name;

//     private String imageName;

//     private Boolean isActive;

//     @ManyToOne
//     @JoinColumn(name = "category_id", nullable = false)
//     private Category category;
// }
