package com.example.shopping.models;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserDtls user;

    @ManyToOne
    private Product product;

    private int quantity;

    private String size;

    @Transient
    private Double totalPrice;

    @Transient
    private Double totalProductPrice;

}
