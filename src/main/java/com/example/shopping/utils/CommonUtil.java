package com.example.shopping.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.example.shopping.models.OrderItem;
import com.example.shopping.models.ProductOrder;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender;

    public Boolean sendMail(String url, String recipientEmail) throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        // Thiết lập người gửi và tên hiển thị
        helper.setFrom("nguyenduybao312@gmail.com", "Cửa hàng thời trang NDB");
        // Thiết lập người nhận email
        helper.setTo(recipientEmail);

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
        + "<p>&copy; 2025 Cửa hàng thời trang NDB</p>"
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("nguyenduybao312@gmail.com", "Cửa hàng thời trang NDB");
            helper.setTo(productOrder.getUser().getEmail());

            // Tạo hóa đơn PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Tiêu đề
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("HÓA ĐƠN ĐẶT HÀNG - CỬA HÀNG NDB", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Thông tin đơn hàng
            document.add(new Paragraph("Mã đơn hàng: " + productOrder.getOrderId()));
            document.add(new Paragraph("Ngày đặt: " + productOrder.getOrderedDay()));
            document.add(new Paragraph(" "));

            // Bảng sản phẩm dùng PdfPTable thay vì Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1f, 2f, 2f});
            table.getDefaultCell().setPadding(5);

            // Header
            String[] headers = {"Tên sản phẩm", "Số lượng", "Giá", "Tổng giá"};
            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(5);
                table.addCell(headerCell);
            }

            // Dữ liệu
            for (OrderItem item : productOrder.getItems()) {
                PdfPCell cell1 = new PdfPCell(new Phrase(item.getProduct().getTitle()));
                cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell1.setPadding(5);
                table.addCell(cell1);

                PdfPCell cell2 = new PdfPCell(new Phrase(String.valueOf(item.getQuantity())));
                cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell2.setPadding(5);
                table.addCell(cell2);

                PdfPCell cell3 = new PdfPCell(new Phrase(formatCurrency(item.getPrice())));
                cell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell3.setPadding(5);
                table.addCell(cell3);

                PdfPCell cell4 = new PdfPCell(new Phrase(formatCurrency(item.getPrice() * item.getQuantity())));
                cell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell4.setPadding(5);
                table.addCell(cell4);
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Thông tin thanh toán
            document.add(new Paragraph("Tiền giảm: " +
                    ((productOrder.getDiscount() != null && productOrder.getDiscount() > 0)
                            ? formatCurrency(productOrder.getDiscount()) : "Không có")));

            document.add(new Paragraph("Phí vận chuyển: " + formatCurrency(productOrder.getDeliveryPrice())));
            document.add(new Paragraph("Tổng cộng: " + formatCurrency(productOrder.getTotalOrderPrice())));
            document.add(new Paragraph("Thanh toán: " + productOrder.getPaymentType()));

            document.close();

            // Gửi email
            helper.setSubject("Hóa đơn đơn hàng #" + productOrder.getOrderId());
            helper.setText(
                    "<p>Xin chào,</p>" +
                            "<p>Cảm ơn bạn đã đặt hàng tại <strong>Cửa hàng thời trang NDB</strong>.</p>" +
                            "<p>Vui lòng xem hóa đơn đính kèm.</p>" +
                            "<p>Trân trọng,<br/>NDB Fashion Store</p>", true);

            helper.addAttachment("HoaDon_" + productOrder.getOrderId() + ".pdf",
                    new ByteArrayDataSource(baos.toByteArray(), "application/pdf"));

            mailSender.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Định dạng số tiền: thêm dấu phẩy, bỏ .0 nếu là số nguyên.
     * Ví dụ: 1500000.0 ➝ 1,500,000 Đ
     */
    private String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,##0.##");
        return formatter.format(amount) + " Đ";
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
