package com.example.shopping.services;

import java.util.List;

import com.example.shopping.models.Cart;

public interface CartService {
    public Cart saveCart(int productId, int userId);

    public List<Cart> getCartsByUser(int userId);

    public int getCountCart(int userId);

    public void updateQuantity(String sy, int cid);

    public void deleteCart(int cartId);
}
