package com.example.shopping.utils;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.example.shopping.models.OrderItem;
import com.example.shopping.models.ProductOrder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

// Lớp tiện ích để hỗ trợ các chức năng chung cho ứng dụng
@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender; // Đối tượng gửi mail

    // Phương thức gửi email khôi phục mật khẩu
    public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException{

        // Tạo đối tượng MimeMessage để thiết lập nội dung email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        // Thiết lập người gửi và tên hiển thị
        helper.setFrom("nguyenduybao312@gmail.com", "Cửa hàng thời trang NDB");
        // Thiết lập người nhận email
        helper.setTo(reciepentEmail);

        // Tạo nội dung email với HTML bao gồm liên kết khôi phục mật khẩu
        String content = "<html><head><style>"
        + "body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; background-color: #f4f4f4; }"
        + "table { width: 100%; border-collapse: collapse; margin-top: 20px; background-color: #fff; }"
        + "th, td { padding: 10px; border: 1px solid #ddd; text-align: left; }"
        + "th { background-color: #007bff; color: white; font-weight: bold; }"
        + "td { font-size: 14px; }"
        + "h1, h2 { color: #333; font-size: 22px; font-weight: bold; }"
        + "p { font-size: 14px; color: #333; margin-bottom: 15px; }"
        + "a { color: #007bff; text-decoration: none; }"
        + "a:hover { text-decoration: underline; }"
        + ".order-summary { margin-top: 20px; font-size: 16px; font-weight: bold; color: #333; }"
        + ".footer { font-size: 12px; color: #888; text-align: center; margin-top: 30px; padding: 15px; background-color: #f4f4f4; }"
        + ".total { font-size: 18px; font-weight: bold; color: #2d2d2d; margin-top: 10px; }"
        + ".highlight { color: #e74c3c; font-weight: bold; }"
        + "</style></head><body>"
        + "<div style='width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px;'>"
        + "<h1>Cửa hàng thời trang NDB xin chào!</h1>"
        + "<p>Bạn đã yêu cầu gửi code khôi phục mật khẩu. Vui lòng nhấn vào đường link dưới đây để vào trang khôi phục mật khẩu:</p>"
        + "<p><a href=\"" + url + "\">Thay đổi mật khẩu</a></p>"
        + "<div class='footer'>"
        + "<p>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</p>"
        + "<p>&copy; 2024 Cửa hàng thời trang NDB</p>"
        + "</div>"
        + "</div>"
        + "</body></html>";

        // Thiết lập tiêu đề email và nội dung với định dạng HTML
        helper.setSubject("Password Reset");
        helper.setText(content, true); // 'true' để cho phép HTML
        mailSender.send(message); // Gửi email

        return true; // Trả về true nếu gửi thành công
    }

    // Phương thức tạo URL gốc của ứng dụng
    public static String generateUrl(HttpServletRequest request) {
        // Lấy URL hiện tại và loại bỏ phần servlet path để lấy URL gốc
        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), ""); // Trả về URL gốc
    }

    public Boolean sendOrderMail(ProductOrder productOrder, String status) {
        try {
            // Tạo đối tượng MimeMessage để thiết lập nội dung email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
    
            // Thiết lập người gửi và tên hiển thị
            helper.setFrom("nguyenduybao312@gmail.com", "Cửa hàng thời trang NDB");
    
            // Thiết lập người nhận email
            helper.setTo(productOrder.getUser().getEmail());
    
            // Tạo nội dung email tùy theo trạng thái
            StringBuilder msg = new StringBuilder();
    
            // CSS phần email thông báo đơn hàng
            String css = "<style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; background-color: #f4f4f4; }"
                    + "h1, h2 { color: #333; font-size: 24px; font-weight: bold; }"
                    + "p { font-size: 14px; color: #333; margin-bottom: 15px; }"
                    + "table { width: 100%; border-collapse: collapse; background-color: #fff; margin-top: 20px; }"
                    + "th, td { padding: 10px; border: 1px solid #ddd; text-align: left; font-size: 14px; }"
                    + "th { background-color: #007bff; color: white; font-weight: bold; }"
                    + "td { font-size: 14px; color: #333; }"
                    + ".footer { font-size: 12px; color: #888; text-align: center; margin-top: 30px; padding: 15px; background-color: #f4f4f4; }"
                    + ".highlight { color: #e74c3c; font-weight: bold; }"
                    + "</style>";
    
            msg.append(css);
            
            if ("Đang xử lý".equals(status)) {
                msg.append("<h1>Cảm ơn bạn đã đặt hàng tại cửa hàng thời trang NDB!</h1>")
                   .append("<p>Mã đơn hàng: <strong>").append(productOrder.getOrderId()).append("</strong></p>")
                   .append("<table border='1' cellpadding='5' cellspacing='0' style='width: 100%; background-color: #f9f9f9;'>")
                   .append("<tr><th style='background-color: #007bff; color: white;'>Tên sản phẩm</th><th style='background-color: #007bff; color: white;'>Số lượng</th><th style='background-color: #007bff; color: white;'>Giá</th><th style='background-color: #007bff; color: white;'>Tổng giá</th></tr>");
    
                // Duyệt qua danh sách sản phẩm trong đơn hàng
                for (OrderItem item : productOrder.getItems()) {
                    msg.append("<tr>")
                       .append("<td>").append(item.getProduct().getTitle()).append("</td>")
                       .append("<td>").append(item.getQuantity()).append("</td>")
                       .append("<td>").append(item.getPrice()).append(" Đ</td>")
                       .append("<td>").append(item.getPrice() * item.getQuantity()).append(" Đ</td>")
                       .append("</tr>");
                }
    
                msg.append("</table>");

                if (productOrder.getDiscount() != null && productOrder.getDiscount() > 0) {
                    msg.append("<p>Tiền giảm: <strong>").append(productOrder.getDiscount()).append(" Đ</strong></p>");
                } else {
                    msg.append("<p>Tiền giảm: <strong>Không có</strong></p>");
                }
                
                msg.append("<p>Tiền vận chuyển: <strong>").append(productOrder.getDeliveryPrice()).append(" Đ</strong></p>")
                   .append("<p>Tổng giá trị: <strong>").append(productOrder.getTotalOrderPrice()).append(" Đ</strong></p>")
                   .append("<p>Phương thức thanh toán: <strong>").append(productOrder.getPaymentType()).append("</strong></p>")
                   .append("<p>Chúng tôi sẽ xử lý đơn hàng của bạn ngay lập tức!</p>");
                
                helper.setSubject("Hóa Đơn Đặt Hàng - Đang xử lý");                
            } else if ("Hủy".equals(status)) {
                // Nội dung email cho trạng thái "Hủy"
                msg.append("<p>Xin chào!</p>")
                   .append("<p>Chúng tôi rất tiếc phải thông báo rằng đơn hàng của bạn đã bị hủy.</p>")
                   .append("<p>Đơn hàng mã: <strong>").append(productOrder.getOrderId()).append("</strong></p>")
                   .append("<p>Hy vọng có cơ hội phục vụ bạn trong tương lai!</p>");
    
                helper.setSubject("Thông Báo Hủy Đơn Hàng");
            }
    
            helper.setText(msg.toString(), true);
            mailSender.send(message);
            return true; 
        } catch (UnsupportedEncodingException | MessagingException e) {
            return false;
        }
    }
        
    // Phương thức gửi OTP qua email
    public Boolean sendOtpEmail(String recipientEmail, String otp) throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
    
        helper.setFrom("nguyenduybao312@gmail.com", "Cửa hàng thời trang NDB");
        helper.setTo(recipientEmail);
    
        // CSS cho email OTP
        String css = "<style>"
                + "body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; background-color: #f4f4f4; }"
                + "p { font-size: 14px; color: #333; margin-bottom: 15px; }"
                + "h2 { font-size: 22px; font-weight: bold; color: #007bff; }"
                + "strong { color: #e74c3c; font-weight: bold; }"
                + ".footer { font-size: 12px; color: #888; text-align: center; margin-top: 30px; padding: 15px; background-color: #f4f4f4; }"
                + "</style>";
    
        String content = css + "<p>Cửa hàng thời trang NDB xin chào!</p>"
                + "<p>Bạn đã đăng ký tài khoản tại cửa hàng của chúng tôi.</p>"
                + "<p>Mã OTP của bạn để xác nhận tài khoản là: <strong>" + otp + "</strong></p>"
                + "<p>Vui lòng nhập mã OTP vào hệ thống để kích hoạt tài khoản của bạn.</p>";
    
        helper.setSubject("Mã OTP xác nhận tài khoản");
        helper.setText(content, true);
        mailSender.send(message);
    
        return true; 
    }    
    
}
