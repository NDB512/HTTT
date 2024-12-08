package com.example.shopping.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.shopping.models.Coupon;
import com.example.shopping.models.UserCoupon;
import com.example.shopping.models.UserDtls;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Integer> {
    UserCoupon findByUserAndCoupon(UserDtls user, Coupon coupon);
}
