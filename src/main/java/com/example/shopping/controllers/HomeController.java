package com.example.shopping.controllers;

import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.shopping.models.UserDtls;
import com.example.shopping.models.Category;
import com.example.shopping.models.Product;
import com.example.shopping.services.*;
import com.example.shopping.utils.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;

    @GetMapping("/403")
    public String accessDenied() {
        return "403"; // Trả về tệp 403.html
    }

    // Phương thức này được gọi trước mỗi yêu cầu để lấy thông tin người dùng và danh mục
    @ModelAttribute
    public void getUserDetails(Principal principal, Model model) {
        if (principal != null) {
            // Lấy email từ thông tin xác thực của người dùng và truy vấn thông tin từ database
            String email = principal.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            model.addAttribute("user", userDtls); // Đưa thông tin người dùng vào Model

            // Đếm số lượng trong giỏ hàng tạo ra bởi user
            int countCart = cartService.getCountCart(userDtls.getId());
            model.addAttribute("countCart", countCart);
        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        model.addAttribute("categorys", allActiveCategory); // Đưa danh mục vào Model
    }

    // Trang chủ
    @GetMapping("/")
    public String index(Model model) {
        List<Category> allActiveCategories = categoryService.getAllActiveCategory().stream().sorted((p1, p2) -> Integer.compare(p2.getId(), p1.getId())) .limit(6).toList();
        List<Product> allActiveProducts = productService.getAllActiveProduct("").stream()
            .sorted((p1, p2) -> Integer.compare(p2.getId(), p1.getId())) 
            .limit(4).toList();

        model.addAttribute("products", allActiveProducts);
        model.addAttribute("categories", allActiveCategories);
        return "index";
    }

    // Trang đăng nhập
    @GetMapping("/signin")
    public String login() {
        return "login";
    }

    // Trang đăng ký
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/contact")
    public String contant(){
        return "contact";
    }

    @GetMapping("/about-us")
    public String about_us(){
        return "about_us";
    }

    @GetMapping("/privacy-policy")
    public String privacy_policy(){
        return "privacy_policy";
    }

    @GetMapping("/terms-of-service")
    public String terms_of_service(){
        return "terms_of_service";
    }

    // Trang sản phẩm, hiển thị các sản phẩm theo danh mục nếu có
    @GetMapping("/products")
    public String product(Model model, 
                          @RequestParam(defaultValue = "") String category,
                          @RequestParam(defaultValue = "") String ch,  // Thêm tham số tìm kiếm
                          @RequestParam(defaultValue = "0") int pageNo,
                          @RequestParam(defaultValue = "4") int pageSize) {
        
        List<Category> categories = categoryService.getAllActiveCategory();
        model.addAttribute("categories", categories);
        model.addAttribute("paramValue", category);
        
        Page<Product> productPage;
        
        // Kiểm tra nếu có từ khóa tìm kiếm
        if (ch != null && !ch.isEmpty()) {
            // Tìm kiếm sản phẩm theo từ khóa
            productPage = productService.searchProductPage(pageNo, pageSize, ch);
            model.addAttribute("searchQuery", ch);  // Lưu từ khóa tìm kiếm vào model để hiển thị trong view
        } else {
            // Nếu không tìm kiếm, lấy danh sách sản phẩm theo danh mục
            productPage = productService.getAllActiveProductPage(pageNo, pageSize, category);
        }
    
        List<Product> products = productPage.getContent();
        
        // Thêm các thông tin phân trang vào model
        model.addAttribute("products", products);
        model.addAttribute("productsSize", products.size());
        model.addAttribute("pageNo", productPage.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("totalPage", productPage.getTotalPages());
        model.addAttribute("isFirst", productPage.isFirst());
        model.addAttribute("isLast", productPage.isLast());
        
        return "product";
    }

    // Trang xem chi tiết sản phẩm
    @GetMapping("/viewProduct/{id}")
    public String viewProduct(@PathVariable int id, Model model) {
        // Lấy thông tin sản phẩm theo ID
        Product productById = productService.getProductByItem(id);
        
        // Lấy danh sách sản phẩm liên quan (cùng danh mục với sản phẩm hiện tại)
        List<Product> relatedProducts = productService.getProductsByCategory(productById.getCategory());
        
        // Loại bỏ sản phẩm hiện tại khỏi danh sách sản phẩm liên quan
        relatedProducts.removeIf(product -> product.getId() == id);
    
        // Thêm sản phẩm vào model để hiển thị thông tin chi tiết
        model.addAttribute("product", productById);
        model.addAttribute("relatedProducts", relatedProducts);  // Truyền danh sách sản phẩm liên quan
    
        return "view_product";
    }      

    // Xử lý lưu tài khoản người dùng mới
    @SuppressWarnings("null")
    @PostMapping("/saveUser")
    public String saveAccount(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws Exception {
    
        // Kiểm tra xem email đã tồn tại hay chưa
        Boolean existsEmail = userService.existsEmail(user.getEmail());
    
        if (existsEmail) {
            session.setAttribute("errorMsg", "Email này đã được đăng ký!");
            return "redirect:/register";  // Quay lại trang đăng ký
        } else {
            // Lấy tên ảnh đại diện từ file tải lên hoặc dùng ảnh mặc định
            String imageName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : "default_account.jpg";
            user.setProfileImage(imageName);
    
            // Lưu người dùng vào database và nhận lại người dùng đã lưu
            UserDtls savedUser = userService.saveUser(user);
    
            if (savedUser != null) {
                // Chỉ định thư mục lưu trữ tệp ảnh
                Path directoryPath = Path.of("src/main/resources/static/img/profile_img");
                if (!Files.exists(directoryPath)) {
                    Files.createDirectories(directoryPath); // Tạo thư mục nếu chưa tồn tại
                }
    
                // Nếu ảnh không phải là ảnh mặc định, lưu ảnh vào thư mục đã chỉ định
                if (!"default_account.jpg".equals(imageName)) {
                    Path filePath = directoryPath.resolve(imageName);
                    try {
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        session.setAttribute("errorMsg", "Lưu hình ảnh không thành công: " + e.getMessage());
                        return "redirect:/register"; // Quay lại trang đăng ký nếu có lỗi
                    }
                }
    
                // Hiển thị thông báo thành công và yêu cầu người dùng kiểm tra email
                session.setAttribute("succMsg", "Tạo tài khoản thành công, vui lòng kiểm tra email để nhận mã OTP!");
                return "redirect:/register";  // Giữ người dùng lại trang đăng ký, hiển thị form nhập OTP
            } else {
                session.setAttribute("errorMsg", "LỖI SERVER !!! Không tạo tài khoản được !");
                return "redirect:/register";  // Quay lại trang đăng ký nếu có lỗi
            }
        }
    }
    
    @PostMapping("/verifyOtp")
    public String verifyOtp(@RequestParam String otp, HttpSession session) {
        // Gọi Service để xác minh OTP
        UserDtls user = userService.verifyOtp(otp);
    
        if (user != null) {
            // Nếu OTP hợp lệ và tài khoản được kích hoạt
            session.setAttribute("succMsg", "Tài khoản đã được kích hoạt thành công!");
            return "redirect:/signin";  // Chuyển hướng đến trang đăng nhập
        } else {
            // Nếu OTP không hợp lệ hoặc đã hết hạn
            session.setAttribute("errorMsg", "Mã OTP không hợp lệ hoặc đã hết hạn!");
            return "redirect:/register";  // Quay lại trang đăng ký và hiển thị form nhập OTP
        }
    }    

    //Sử lý sự kiện quên mật khẩu
    @GetMapping("/forgot-password")
    public String showForgotPassword(){
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgetPassword(@RequestParam String email, @RequestParam String phoneNumber, HttpSession session, HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
        UserDtls userByEmail = userService.getUserByEmail(email);
    
        // Kiểm tra xem email có tồn tại trong cơ sở dữ liệu không
        if (ObjectUtils.isEmpty(userByEmail)) {
            session.setAttribute("errorMsg", "Không tìm thấy email !");
        } else if (!userByEmail.getPhoneNumber().equals(phoneNumber)) {
            // Kiểm tra xem số điện thoại người dùng nhập có khớp với số điện thoại trong cơ sở dữ liệu không
            session.setAttribute("errorMsg", "Số điện thoại không đúng !");
        } else {
            String resetToken = UUID.randomUUID().toString();
            userService.updateUserResetToken(email, resetToken);
    
            String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;
    
            Boolean sendMail = commonUtil.sendMail(url, email);
    
            if (sendMail) {
                session.setAttribute("succMsg", "Đã gửi mã sang mail, hãy xem mail của bạn !");
            } else {
                session.setAttribute("errorMsg", "Server bị lỗi, không gửi được mail, vui lòng thử lại sau !");
            }
        }
    
        return "redirect:/forgot-password";
    }
    

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token, HttpSession session, Model model){

        UserDtls userByToken = userService.getUserByToken(token);
        if(userByToken==null){
            model.addAttribute("msg", "Link khôi phục mật khẩu bị lỗi hoặc vô hiệu hóa, xin hãy thử lại !");
            return "message";
        }
        model.addAttribute("token", token);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session, Model model){
        UserDtls userByToken = userService.getUserByToken(token);
        
        if(userByToken==null){
            model.addAttribute("msg", "Link khôi phục mật khẩu bị lỗi hoặc vô hiệu hóa, xin hãy thử lại !");
            return "message";
        } else {
            userByToken.setPassword(passwordEncoder.encode(password));
            userByToken.setResetToken(null);
            userByToken.setFailedAttempt(0);
            userService.updateUser(userByToken);
            //model.addAttribute("succMsg", "Mật khẩu thay đổi thành công !");
            model.addAttribute("msg", "Mật khẩu thay đổi thành công !");
            return "message";
        }
    }

}
