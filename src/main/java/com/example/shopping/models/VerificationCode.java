package com.example.shopping.models;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String otp;
    private String email;

    @OneToOne
    private UserDtls user;

    private LocalDateTime createdAt;

    public boolean isExpired() {
        if (createdAt == null) {
            return true;
        }
        return createdAt.plus(5, ChronoUnit.MINUTES).isBefore(LocalDateTime.now());
    }
}
