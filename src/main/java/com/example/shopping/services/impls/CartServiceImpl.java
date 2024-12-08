package com.example.shopping.services.impls;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.shopping.models.Cart;
import com.example.shopping.models.Product;
import com.example.shopping.models.UserDtls;
import com.example.shopping.repositories.*;
import com.example.shopping.services.CartService;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart saveCart(int productId, int userId) {
        UserDtls userDtls = userRepository.findById(userId).get();
        Product product = productRepository.findById(productId).get();
    
        Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);
    
        Cart cart = null;
        if (ObjectUtils.isEmpty(cartStatus)) {
            cart = new Cart();
            cart.setProduct(product);
            cart.setUser(userDtls);
            cart.setQuantity(1);
            
            // Tính tổng giá ban đầu của sản phẩm (bao gồm cả giảm giá nếu có)
            Double productPrice = product.getFinalPrice();
            if (product.getDiscount() != null && product.getDiscount() > 0) {
                productPrice = productPrice * (1 - product.getDiscount() / 100);
            }
            
            cart.setTotalPrice(cart.getQuantity() * productPrice);
        } else {
            cart = cartStatus;
            cart.setQuantity(cart.getQuantity() + 1);
    
            // Tính tổng giá của sản phẩm sau khi cập nhật số lượng
            Double productPrice = product.getFinalPrice();
            if (product.getDiscount() != null && product.getDiscount() > 0) {
                productPrice = productPrice * (1 - product.getDiscount() / 100);
            }
    
            cart.setTotalPrice(cart.getQuantity() * productPrice);
        }
    
        // Giảm số lượng sản phẩm trong kho
        product.setStock(product.getStock() - 1);
    
        Cart saveCart = cartRepository.save(cart);
        return saveCart;
    }    

    @Override
    public List<Cart> getCartsByUser(int userId) {
        List<Cart> carts = cartRepository.getCartsByUser(userId);
        List<Cart> updateCart = new ArrayList<>();
        double totalProductPrice = 0.0;
    
        for (Cart c : carts) {
            Product product = c.getProduct();
            Double productPrice = product.getFinalPrice();
            
            // Tính giá sau giảm nếu có
            if (product.getDiscount() != null && product.getDiscount() > 0) {
                productPrice = productPrice * (1 - product.getDiscount() / 100);
            }
    
            // Cập nhật lại tổng giá sản phẩm trong giỏ
            Double totalPrice = productPrice * c.getQuantity();
            c.setTotalPrice(totalPrice);
            totalProductPrice += totalPrice;
    
            // Cập nhật tổng giá giỏ hàng
            c.setTotalProductPrice(totalProductPrice);
    
            updateCart.add(c);
        }
    
        return updateCart;
    }

    @Override
    public int getCountCart(int userId) {
        int countByUserId = cartRepository.countByUserId(userId);
        return countByUserId;
    }

    @Override
    public void updateQuantity(String sy, int cid) {
        Cart cart = cartRepository.findById(cid).orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));
        Product product = cart.getProduct(); // Lấy sản phẩm từ giỏ hàng
        int updateQuantity;
    
        if(sy.equalsIgnoreCase("de")) {
            updateQuantity = cart.getQuantity() - 1;
            if(updateQuantity <= 0) {
                cartRepository.delete(cart); // Xóa sản phẩm khỏi giỏ hàng
            } else {
                cart.setQuantity(updateQuantity);
                cartRepository.save(cart); // Cập nhật giỏ hàng
            }
            // Trả hàng vào kho
            product.setStock(product.getStock() + 1); // Tăng số lượng trong kho
            productRepository.save(product); // Lưu thay đổi kho hàng
        } else {
            // Kiểm tra số lượng trong kho trước khi tăng số lượng
            if (cart.getQuantity() > product.getStock()) {
                throw new RuntimeException("Số lượng yêu cầu lớn hơn số lượng trong kho");
            }
            updateQuantity = cart.getQuantity() + 1;
            cart.setQuantity(updateQuantity);
            cartRepository.save(cart); // Cập nhật giỏ hàng
            // Giảm hàng trong kho
            product.setStock(product.getStock() - 1); // Giảm số lượng trong kho
            productRepository.save(product); // Lưu thay đổi kho hàng
        }
    }

    @Override
    public void deleteCart(int cartId) {
        cartRepository.deleteById(cartId); // Gọi phương thức xóa theo ID
    }
}
