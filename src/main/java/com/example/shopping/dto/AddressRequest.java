package com.example.shopping.dto;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {
    private UUID id;
    private String phoneNumber;
    private String name;
    private String street;       // Tên đường
    private String ward;         // Phường/Xã
    private String district;     // Quận/Huyện
    private String city;         // Tỉnh/Thành phố
    private String postalCode;   // Mã bưu chính
}
