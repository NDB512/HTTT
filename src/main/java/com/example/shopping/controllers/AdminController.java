package com.example.shopping.controllers;

import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.shopping.dto.CouponDto;
import com.example.shopping.dto.SalesReportDto;
import com.example.shopping.models.Category;
import com.example.shopping.models.Coupon;
import com.example.shopping.models.Product;
import com.example.shopping.models.ProductOrder;
import com.example.shopping.models.UserDtls;
import com.example.shopping.models.WarehouseReceiptForm;
import com.example.shopping.services.*;
import com.example.shopping.utils.CommonUtil;
import com.example.shopping.utils.OrderEnum;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    public CategoryService categoryService;

    @Autowired
    public ProductService productService;

    @Autowired
    public UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private WarehouseReceiptService warehouseReceiptService;

    @GetMapping("/")
    public String admin(){
        return "admin/index";
    }

    @ModelAttribute
    public void getUserDetails(Principal principal, Model model){
        if(principal!=null){
            String email = principal.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            model.addAttribute("user", userDtls);

            // Đếm số lượng trong giỏ hàng tạo ra bởi user
            int countCart = cartService.getCountCart(userDtls.getId());
            model.addAttribute("countCart", countCart);
        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        model.addAttribute("categorys", allActiveCategory);
    }

    //Phần category
    @GetMapping("/category")
    public String category(Model model, 
                           @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "5") int pageSize) {
    
        Page<Category> page = categoryService.getAllCategoryPage(pageNo, pageSize);
        List<Category> categories = page.getContent();
    
        model.addAttribute("categories", categories);
        model.addAttribute("categoriesSize", categories.size());
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
    
        return "admin/category";
    }    

    @SuppressWarnings("null")
    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category, @RequestParam MultipartFile file, HttpSession session) throws IOException {

        // Kiểm tra tệp hình ảnh và đặt tên mặc định nếu không có tệp
        String imageName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : "default.jpg";
        category.setImageName(imageName);
    
        // Kiểm tra sự tồn tại của danh mục
        Boolean existCategory = categoryService.existCategory(category.getName());
    
        if (existCategory) {
            session.setAttribute("errorMsg", "Danh mục đã tồn tại !");
        } else {
            Category savedCategory = categoryService.saveCategory(category);
            if (ObjectUtils.isEmpty(savedCategory)) {
                session.setAttribute("errorMsg", "Lưu thất bại! Lỗi server !");
            } else {
                // Chỉ định thư mục lưu trữ tệp
                Path directoryPath = Paths.get("src/main/resources/static/img/category_img");
                if (!Files.exists(directoryPath)) {
                    Files.createDirectories(directoryPath); // Tạo thư mục nếu chưa tồn tại
                }
    
                // Đường dẫn đầy đủ của tệp cần lưu
                Path filePath = directoryPath.resolve(imageName);
    
                // Chỉ lưu ảnh mới nếu không phải là ảnh mặc định
                if (!"default.jpg".equals(imageName)) {
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                }
    
                session.setAttribute("succMsg", "Lưu thành công!");
            }
        }
    
        return "redirect:/admin/category";
    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session){
        
        Boolean deleteCategory = categoryService.deleteCategory(id);
        if(deleteCategory){
            session.setAttribute("succMsg", "Xóa danh mục thành công !");
        } else {
            session.setAttribute("errorMsg", "Server bị lỗi !");
        }

        return "redirect:/admin/category";
    }

    @GetMapping("/editCategory/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editCategory(@PathVariable int id, Model model){
        model.addAttribute("category", categoryService.getCategoryById(id));

        return "admin/edit_category";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) {
        Category oldCategory = categoryService.getCategoryById(category.getId());
        String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
        String uploadDir = "src/main/resources/static/img/category_img";
        
        // Cập nhật thông tin danh mục
        oldCategory.setName(category.getName());
        oldCategory.setIsActive(category.getIsActive());
        oldCategory.setImageName(imageName);
        
        // Lưu hình ảnh nếu có
        if (!file.isEmpty()) {
            try {
                // Tạo thư mục nếu chưa tồn tại
                Files.createDirectories(Paths.get(uploadDir));
    
                // Xóa hình ảnh cũ nếu không phải ảnh mặc định
                if (!"default.jpg".equals(oldCategory.getImageName())) {
                    Files.deleteIfExists(Paths.get(uploadDir, oldCategory.getImageName()));
                }
    
                // Lưu tệp hình ảnh mới vào thư mục tĩnh
                file.transferTo(Paths.get(uploadDir, imageName));
            } catch (IOException e) {
                e.printStackTrace();
                session.setAttribute("errorMsg", "Lưu hình ảnh không thành công !!!");
                return "redirect:/admin/editCategory/" + category.getId();
            }
        }
    
        // Lưu danh mục
        Category updateCategory = categoryService.saveCategory(oldCategory);
        session.setAttribute("succMsg", updateCategory != null ? "Cập nhập danh mục thành công !!!" : "Cập nhập danh mục thất bại !!!");
    
        return "redirect:/admin/editCategory/" + category.getId();
    }
        
    //Phần product
    @GetMapping("/addProduct")
    public String addProduct(Model model){
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories",categories);
        return "admin/add_product";
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image, 
                              @RequestParam("category") String categoryName,
                              @RequestParam("productTaxRate") double productTaxRate, 
                              @RequestParam("profitMargin") double profitMargin, 
                              HttpSession session) throws IOException {
    
        // Kiểm tra đầu vào của productTaxRate và profitMargin
        if (productTaxRate < 0 || productTaxRate > 100) {
            session.setAttribute("errorMsg", "Lỗi: Tỷ lệ thuế phải nằm trong khoảng từ 0 đến 100.");
            return "redirect:/admin/addProduct";
        }
    
        if (profitMargin < 0 || profitMargin > 100) {
            session.setAttribute("errorMsg", "Lỗi: Tỷ lệ lợi nhuận phải nằm trong khoảng từ 0 đến 100.");
            return "redirect:/admin/addProduct";
        }
    
        // Kiểm tra xem tên sản phẩm đã tồn tại chưa
        if (productService.isProductTitleExist(product.getTitle())) {
            session.setAttribute("errorMsg", "Lỗi: Sản phẩm với tên này đã tồn tại.");
            return "redirect:/admin/addProduct";
        }
    
        // Kiểm tra tệp hình ảnh và đặt tên mặc định nếu không có tệp
        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
        product.setImage(imageName);
    
        double originalPrice = product.getPrice();
        double taxAmount = originalPrice * (productTaxRate) / 100;
    
        // Tính lợi nhuận mong muốn
        double profitAmount = originalPrice * profitMargin / 100;
    
        // Tính giá cuối cùng
        double finalPrice = originalPrice + taxAmount + profitAmount;
    
        // Đặt giá cuối cùng sau thuế và lợi nhuận
        product.setFinalPrice(finalPrice);
    
        // Cài đặt giá trị cho sản phẩm
        product.setDiscount(0.0);
        product.setCreatedDate(LocalDateTime.now());
        product.setUpdateDate(product.getCreatedDate());
    
        // Lưu sản phẩm
        Product savedProduct = productService.saveProduct(product);
    
        if (!ObjectUtils.isEmpty(savedProduct)) {
            saveImage(image, imageName, session);
            session.setAttribute("succMsg", "Thêm sản phẩm thành công");
        } else {
            session.setAttribute("errorMsg", "LỖI !!! Không thêm sản phẩm được");
        }
    
        return "redirect:/admin/addProduct";
    }    
    
    private void saveImage(MultipartFile image, String imageName, HttpSession session) throws IOException {
        Path directoryPath = Paths.get("src/main/resources/static/img/product_img");
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath); // Tạo thư mục nếu chưa tồn tại
        }
    
        if (!"default.jpg".equals(imageName)) {
            Path filePath = directoryPath.resolve(imageName);
            try {
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                session.setAttribute("errorMsg", "Lưu hình ảnh không thành công: " + e.getMessage());
            }
        }
    }
    
    @GetMapping("/productList")
    public String viewProduct(Model model, @RequestParam(defaultValue = "") String ch,
                            @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize){
                                
        Page<Product> page = null;
        if(ch!=null && ch.length()>0){
            page = productService.searchProductPage(pageNo, pageSize, ch);
        } else {
            page = productService.getAllProductPage(pageNo, pageSize);
        }
        
        model.addAttribute("productList", page.getContent());

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/productList";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session){
        Boolean deleteProduct = productService.deleteProduct(id);
        if(deleteProduct){
            session.setAttribute("succMsg", "Xóa sản phẩm thành công !");
        } else {
            session.setAttribute("errorMsg", "Server bị lỗi ! Không xóa sản phẩm được !");
        }

        return "redirect:/admin/productList";
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(Model model ,@PathVariable int id, HttpSession session){
        model.addAttribute("product", productService.getProductByItem(id));
        model.addAttribute("categories", categoryService.getAllCategories());

        return "admin/edit_product";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image, HttpSession session){


        if (product.getProductTaxRate() < 0 || product.getProductTaxRate() > 100) {
            session.setAttribute("errorMsg", "Tỷ lệ thuế sản phẩm không hợp lệ!");
            return "redirect:/admin/editProduct/" + product.getId();
        }
    
        if (product.getProfitMargin() < 0 || product.getProfitMargin() > 100) {
            session.setAttribute("errorMsg", "Tỷ lệ lợi nhuận không hợp lệ!");
            return "redirect:/admin/editProduct/" + product.getId();
        }

        // Kiểm tra xem tên sản phẩm đã tồn tại chưa
        if (productService.isProductTitleExist(product.getTitle())) {
            session.setAttribute("errorMsg", "Lỗi: Sản phẩm với tên này đã tồn tại.");
            return "redirect:/admin/addProduct";
        }

        product.setUpdateDate(LocalDateTime.now());

        // Kiểm tra nếu phần trăm giảm giá hợp lệ
        if (product.getDiscount() < 0 || product.getDiscount() > 100) {
            session.setAttribute("errorMsg", "Phần trăm giảm giá không hợp lệ !!!");
            return "redirect:/admin/editProduct/" + product.getId();
        } else {
            // Cập nhật sản phẩm với thuế
            Product updatedProduct = productService.updateProduct(product, image);

            if (!ObjectUtils.isEmpty(updatedProduct)) {
                session.setAttribute("succMsg", "Cập nhập sản phẩm thành công !!!");
            } else {
                session.setAttribute("succMsg", "Cập nhập sản phẩm thất bại !!!");
            }
        }

        return "redirect:/admin/editProduct/" + product.getId();
    }

    @GetMapping("/users")
    public String getAllUsers(Model model){
        List<UserDtls> users = userService.getUsers("ROLE_USER");
        List<UserDtls> staff = userService.getUsers("ROLE_STAFF");
        model.addAttribute("staff", staff);
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/updateSts")
    public String updateStatusUserAccount(@RequestParam Boolean status, @RequestParam int id, HttpSession session){
        Boolean f = userService.updateAccountStatus(id, status);
        if(f){
            session.setAttribute("succMsg", "Cập nhập trạng thái thành công !");
        } else {
            session.setAttribute("error", "Cập nhập trạng thái thất bại !");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/orderList")
    public String getAllOrders(Model model, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize){

         
        Page<ProductOrder> page = orderService.getAllOrdersPage(pageNo, pageSize);
        model.addAttribute("orders", page.getContent());
        model.addAttribute("srch", false);

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        
        return "admin/orderList";
    }

    @GetMapping("/orderdetail/{id}")
    public String getOrderDetails(@PathVariable("id") int orderId, Model model) {
        try {
            ProductOrder order = orderService.getOrderById(orderId); // Lấy đơn hàng theo ID
            model.addAttribute("order", order); // Thêm đơn hàng vào mô hình
            return "admin/order_detail"; // Trả về trang chi tiết đơn hàng
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage()); // Thêm thông báo lỗi vào mô hình
            return "admin/error"; // Trả về trang lỗi hoặc trang khác
        }
    }

    @SuppressWarnings("null")
    @PostMapping("/update-order-status")
    public String updateOrderStatus(@RequestParam int id, @RequestParam String st, HttpSession session) {
        
        OrderEnum[] values = OrderEnum.values();
        String status = null;
    
        // Tìm trạng thái của đơn hàng dựa trên giá trị "st" từ client
        for (OrderEnum orderSt : values) {
            if (orderSt.getId() == Integer.parseInt(st)) {
                status = orderSt.getName();
                break;
            }
        }
    
        // Cập nhật trạng thái đơn hàng
        ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
    
        if (!ObjectUtils.isEmpty(updateOrder)) {
            if (status.equals("Hủy")) { // Chỉ gửi email khi trạng thái là "Hủy"
                // Gửi email thông báo hủy đơn hàng
                commonUtil.sendOrderMail(updateOrder, status);
    
                // Cập nhật số lượng tồn kho khi hủy đơn hàng
                orderService.handleStockAfterCancellation(updateOrder);
    
                session.setAttribute("succMsg", "Đã hủy đơn hàng !");
            } else {
                session.setAttribute("succMsg", "Đã cập nhật trạng thái đơn hàng !");
            }
        } else {
            session.setAttribute("errorMsg", "Không thể cập nhật trạng thái đơn hàng !");
        }
    
        // Điều hướng đến trang danh sách đơn hàng sau khi xử lý
        return "redirect:/admin/orderList";
    }    

    @GetMapping("/search-order")
    public String searchProduct(@RequestParam String orderId, Model model, HttpSession session, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
    @RequestParam(name = "pageSize", defaultValue = "5") int pageSize){

        if(orderId!=null && orderId.length()>0){
            ProductOrder order = orderService.getOrderByOrderid(orderId.trim());

            if(ObjectUtils.isEmpty(order)){
                session.setAttribute("errorMsg", "Không tìm thấy Id");
                model.addAttribute("OrderDtls", null);
            } else {
                model.addAttribute("OrderDtls", order);
            }
    
            model.addAttribute("srch", true);
        } else {
            Page<ProductOrder> page = orderService.getAllOrdersPage(pageNo, pageSize);
            model.addAttribute("orders", page.getContent());
            model.addAttribute("srch", false);
    
            model.addAttribute("pageNo", page.getNumber());
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("totalElements", page.getTotalElements());
            model.addAttribute("totalPage", page.getTotalPages());
            model.addAttribute("isFirst", page.isFirst());
            model.addAttribute("isLast", page.isLast());
        }
        return "admin/orderList";   
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/add-staff")
    public String addStaff(){
        return "admin/add_staff";
    }

    @SuppressWarnings("null")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/saveStaff")
    public String saveStaff(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session
    , @RequestParam("confirmPassword") String confirmPassword) throws IOException {

            // Kiểm tra xem mật khẩu và xác nhận mật khẩu có khớp nhau không
        if (!user.getPassword().equals(confirmPassword)) {
            session.setAttribute("errorMsg", "Mật khẩu và xác nhận mật khẩu không khớp!");
            return "redirect:/admin/add-staff";
        }

        // Lấy tên ảnh đại diện từ file tải lên hoặc dùng ảnh mặc định
        String imageName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : "default_account.jpg";
        user.setProfileImage(imageName);

        // Lưu người dùng vào database
        UserDtls savedUser = userService.saveStaff(user);

        if (!ObjectUtils.isEmpty(savedUser)) {
            // Chỉ định thư mục lưu trữ tệp ảnh
            Path directoryPath = Paths.get("src/main/resources/static/img/profile_img");
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
                    return "redirect:/register";
                }
            }

            session.setAttribute("succMsg", "Tạo tài khoản thành công !");
        } else {
            session.setAttribute("errorMsg", "LỖI SERVER !!! Không tạo tài khoản được !");
        }

        return "redirect:/admin/add-staff";
    }

    @GetMapping("/revenue")
    public String revenue(@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
                          @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
                          Model model) {
        // Nếu không nhập thời gian, lấy mặc định 1 năm gần nhất
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(12).withHour(0).withMinute(0).withSecond(0);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        }
    
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedStartDate = startDate.toLocalDate().format(formatter);
        String formattedEndDate = endDate.toLocalDate().format(formatter);
    
        // Lấy dữ liệu báo cáo doanh thu
        List<SalesReportDto> revenueReport = orderService.generateSalesReport(startDate, endDate);
    
        // Đưa dữ liệu vào model để hiển thị trong view
        model.addAttribute("revenueReport", revenueReport);
        model.addAttribute("startDate", formattedStartDate);
        model.addAttribute("endDate", formattedEndDate);
    
        return "/admin/revenue";
    }
    
    @PostMapping("/revenue")
    public String filterRevenue(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                Model model) {
        // Chuyển đổi LocalDate sang LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
    
        // Lấy dữ liệu báo cáo doanh thu từ OrderService theo khoảng thời gian lọc
        List<SalesReportDto> revenueReport = orderService.generateSalesReport(startDateTime, endDateTime);
    
        // Đưa dữ liệu vào model để hiển thị trong view
        model.addAttribute("revenueReport", revenueReport);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
    
        return "/admin/revenue"; // Trả lại view với báo cáo đã lọc
    }
    
    // Hiển thị danh sách phiếu nhập kho
    @GetMapping("/warehouseReportList")
    public String viewWarehouseReceiptsList(Model model, @RequestParam(defaultValue = "") String ch,
        @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
        @RequestParam(name = "pageSize", defaultValue = "5") int pageSize) {
            Page<WarehouseReceiptForm> page = null;

        if(ch!=null && ch.length()>0){
            page = warehouseReceiptService.getSeacrhWarehouseReceiptForm(pageNo, pageSize, ch);
        } else {
            page = warehouseReceiptService.getAllWarehouseReceiptFormPage(pageNo, pageSize);
        }

            // Định dạng ngày cho từng phiếu nhập kho trong danh sách
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        for (WarehouseReceiptForm ware : page.getContent()) {
            // Kiểm tra nếu ngày không phải null trước khi định dạng
            if (ware.getDate() != null) {
                LocalDateTime localDateTime = ware.getDate();
                Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                String formattedDate = formatter.format(date);
                ware.setFormattedDate(formattedDate);
            }
        }

        model.addAttribute("warehouseReportList", page.getContent());

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        
        return "/admin/warehouseReceiptList"; 
    }

    @GetMapping("/warehouseReceiptForm")
    public String warehouseReceipts(){
        return "admin/warehouseReceiptForm";
    }

    @PostMapping("/warehouse-receipts")
    public String saveWarehouseReceipt(
            @ModelAttribute WarehouseReceiptForm receiptForm,
            HttpSession session) {
    
        try {
            // Lưu phiếu nhập kho cùng với danh sách sản phẩm
            warehouseReceiptService.saveWarehouseReceiptForm(receiptForm);
    
            // Thêm thông báo thành công
            session.setAttribute("succMsg", "Phiếu đã được lưu thành công!");
        } catch (Exception e) {
            // Thêm thông báo lỗi
            session.setAttribute("errorMsg", "Đã xảy ra lỗi: " + e.getMessage());
        }
    
        return "redirect:/admin/warehouseReceiptForm"; // Redirect đến trang sau khi lưu
    }

    @GetMapping("/coupons")
    public String couponList(Model model, @RequestParam(defaultValue = "") String ch,
        @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
        @RequestParam(name = "pageSize", defaultValue = "5") int pageSize){
        
        Page<Coupon> page = null;

        if(ch!=null && ch.length()>0){
            page = couponService.searchCouponPage(pageNo, pageSize, ch);
        } else {
            page = couponService.getAllCouponPage(pageNo, pageSize);
        }
        
        model.addAttribute("couponList", page.getContent());

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/couponList";
    }

    @GetMapping("/create-coupon")
    public String showCreateCouponForm(Model model) {
        model.addAttribute("couponDto", new CouponDto());
        return "admin/create-coupon";
    }

    @PostMapping("/save-coupon")
    public String saveCoupon(Model model, @ModelAttribute CouponDto couponDto, HttpSession session){
        try {
            couponService.createCoupon(couponDto);
    
            // Thêm thông báo thành công
            session.setAttribute("succMsg", "Phiếu đã được lưu thành công!");
        } catch (Exception e) {
            // Thêm thông báo lỗi
            session.setAttribute("errorMsg", "Đã xảy ra lỗi: " + e.getMessage());
        }

        return "redirect:/admin/create-coupon";
    }
}
