package com.example.shopping.utils;

import java.util.Random;

public class VerificationCodeGenerator {
    
    public static String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        // Lặp 6 lần để tạo ra 6 chữ số ngẫu nhiên
        for (int i = 0; i < 6; i++) {
            int digit = random.nextInt(10); // Tạo một số ngẫu nhiên từ 0 đến 9
            code.append(digit); // Thêm chữ số vào mã
        }

        return code.toString(); // Trả về chuỗi mã
    }
}
