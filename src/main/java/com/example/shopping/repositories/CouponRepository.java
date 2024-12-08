package com.example.shopping.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.shopping.models.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    Coupon findByCode(String code);

    Page<Coupon> findByCode(String ch, String ch2, Pageable pageable);
}
