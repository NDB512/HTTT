package com.example.shopping.models;

import java.util.Date;
import java.util.List;

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
public class UserDtls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String phoneNumber;

    private String email;

    private String password;

    private String profileImage;

    private String role;

    private Boolean isEnable;

    private Boolean accountNonLock;

    private int failedAttempt;

    private Date lockTime;

    private String resetToken;

    private String otp;
    private Date otpExpirationTime;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Address> addresses; // Danh sách địa chỉ của người dùng
}
