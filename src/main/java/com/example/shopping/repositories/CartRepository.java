package com.example.shopping.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shopping.models.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer>{
    public Cart findByProductIdAndUserId(int productId, int userId);

    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    public List<Cart> getCartsByUser(@Param("userId") int userId);


    public int countByUserId(int userId);
}
