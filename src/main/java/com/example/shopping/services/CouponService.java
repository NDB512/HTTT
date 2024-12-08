package com.example.shopping.services;

import org.springframework.data.domain.Page;

import com.example.shopping.dto.CouponDto;
import com.example.shopping.models.Coupon;
import com.example.shopping.models.UserDtls;

public interface CouponService {
    public Page<Coupon> getAllCouponPage(int pageNo, int pageSize);

    public Page<Coupon> searchCouponPage(int pageNo, int pageSize, String ch);

    public Coupon createCoupon(CouponDto couponDto);

    public Double applyCoupon(UserDtls user, String code, Double orderAmount);

    public Coupon validateCoupon(String code, Double orderAmount);

    public Coupon removeCoupon(UserDtls user, String code);

    public void expireCoupons();
}
