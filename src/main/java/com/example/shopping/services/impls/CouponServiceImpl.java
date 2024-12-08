package com.example.shopping.services.impls;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.shopping.dto.CouponDto;
import com.example.shopping.models.Coupon;
import com.example.shopping.models.CouponStatus;
import com.example.shopping.models.UserCoupon;
import com.example.shopping.models.UserDtls;
import com.example.shopping.repositories.CouponRepository;
import com.example.shopping.repositories.UserCouponRepository;
import com.example.shopping.services.CouponService;

@Service
public class CouponServiceImpl implements CouponService{

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Override
    public Page<Coupon>  getAllCouponPage(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return couponRepository.findAll(pageable);
    }

    @Override
    public Page<Coupon> searchCouponPage(int pageNo, int pageSize, String ch) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return couponRepository.findByCode(ch, ch, pageable);
    }

    @Override
    public Coupon createCoupon(CouponDto couponDto) {
        // Chuyển đổi từ CouponDto sang Coupon
        Coupon coupon = Coupon.builder()
                .code(couponDto.getCode())
                .discountAmount(couponDto.getDiscountAmount())
                .discountPercentage(couponDto.getDiscountPercentage())
                .startDate(couponDto.getStartDate())
                .endDate(couponDto.getEndDate())
                .usageLimit(couponDto.getUsageLimit())
                .status(couponDto.getStatus())
                .minimumOrderAmount(couponDto.getMinimumOrderAmount())
                .build();

        // Lưu coupon vào cơ sở dữ liệu
        return couponRepository.save(coupon);
    }

    @Override
    public Coupon validateCoupon(String code, Double orderAmount) {
        Coupon coupon = couponRepository.findByCode(code);
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon not found.");
        }
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new IllegalArgumentException("Coupon is not active.");
        }
        if (coupon.getStartDate().isAfter(LocalDateTime.now()) || coupon.getEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Coupon is expired.");
        }
        if (orderAmount < coupon.getMinimumOrderAmount()) {
            throw new IllegalArgumentException("Order amount does not meet the minimum requirement.");
        }
        return coupon;
    }

    @Override
    public Double applyCoupon(UserDtls user, String code, Double orderAmount) {
        Coupon coupon = validateCoupon(code, orderAmount);
        UserCoupon userCoupon = userCouponRepository.findByUserAndCoupon(user, coupon);
        if (userCoupon != null) {
            throw new IllegalArgumentException("Coupon has already been used by this user.");
        }

        Double discountAmount = 0.0;
        if (coupon.getDiscountAmount() != null) {
            discountAmount += coupon.getDiscountAmount();
        } 
        
        if (coupon.getDiscountPercentage() != null) {
            discountAmount += orderAmount * coupon.getDiscountPercentage() / 100;
        }
    
        // Đảm bảo số tiền giảm không vượt quá giá trị đơn hàng
        if (discountAmount > orderAmount) {
            discountAmount = orderAmount;
        }
        // Ghi nhận việc sử dụng coupon
        userCoupon = new UserCoupon();
        userCoupon.setUser(user);
        userCoupon.setCoupon(coupon);
        userCoupon.setUsedAt(LocalDateTime.now());
        userCouponRepository.save(userCoupon);
    
        return discountAmount; // Trả về giá giảm
    }
    

    @Override
    public Coupon removeCoupon(UserDtls user, String code) {
        Coupon coupon = couponRepository.findByCode(code);
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon not found.");
        }
        UserCoupon userCoupon = userCouponRepository.findByUserAndCoupon(user, coupon);
        if (userCoupon == null) {
            throw new IllegalArgumentException("Coupon not used by this user.");
        }
        userCouponRepository.delete(userCoupon);
        return coupon;
    }

    @Override
    public void expireCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        for (Coupon coupon : coupons) {
            if (coupon.getEndDate().isBefore(LocalDateTime.now())) {
                coupon.setStatus(CouponStatus.EXPIRED);
                couponRepository.save(coupon);
            }
        }
    }
    
}