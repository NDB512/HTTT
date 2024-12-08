package com.example.shopping.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "addresses")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Address {
    @Id
    @GeneratedValue
    private UUID id;

    private String street;       // Tên đường
    private String ward;         // Phường/Xã
    private String district;     // Quận/Huyện
    private String city;         // Tỉnh/Thành phố
    private String postalCode;   // Mã bưu chính
    private String phoneNumber;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private UserDtls user;
}
