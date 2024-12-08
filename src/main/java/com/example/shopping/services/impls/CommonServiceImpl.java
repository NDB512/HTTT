package com.example.shopping.services.impls;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.shopping.services.CommonService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

// Đánh dấu lớp là một bean dịch vụ, để Spring quản lý và có thể sử dụng ở các lớp khác
@Service
public class CommonServiceImpl implements CommonService {

    // Phương thức này dùng để xóa các thông báo session (như thành công và lỗi) nếu có
    @Override
    public void removeSessionMessage() {
        // Lấy các thuộc tính của yêu cầu HTTP hiện tại
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        // Kiểm tra nếu tồn tại các thuộc tính yêu cầu
        if (attributes != null) {
            // Lấy đối tượng HttpServletRequest từ các thuộc tính yêu cầu
            HttpServletRequest request = attributes.getRequest();
            
            // Lấy session hiện tại của yêu cầu, với false để không tạo session mới nếu chưa có
            HttpSession httpSession = request.getSession(false);

            // Nếu session tồn tại, xóa các thông báo thành công và lỗi
            if (httpSession != null) {
                httpSession.removeAttribute("succMsg"); // Xóa thuộc tính "succMsg" (thông báo thành công)
                httpSession.removeAttribute("errorMsg"); // Xóa thuộc tính "errorMsg" (thông báo lỗi)
            }
        }
    }
}
