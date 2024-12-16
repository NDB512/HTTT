package com.example.shopping.controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.shopping.dto.AddressRequest;
import com.example.shopping.models.Address;
import com.example.shopping.models.Cart;
import com.example.shopping.models.Category;
import com.example.shopping.models.OrderRequest;
import com.example.shopping.models.Product;
import com.example.shopping.models.ProductOrder;
import com.example.shopping.models.UserDtls;
import com.example.shopping.services.AddressService;
import com.example.shopping.services.CartService;
import com.example.shopping.services.CategoryService;
import com.example.shopping.services.CommonService;
import com.example.shopping.services.CouponService;
import com.example.shopping.services.OrderService;
import com.example.shopping.services.UserService;
import com.example.shopping.utils.CommonUtil;
import com.example.shopping.utils.OrderEnum;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CommonService commonService;

    @Autowired 
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CouponService couponService;
    

    @GetMapping("/")
    public String Home(){
        return "user/home";
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

    // Cart của user
    @GetMapping("/addCart")
    public String addToCart(@RequestParam int pid ,@RequestParam int uid, HttpSession session){
        commonService.removeSessionMessage();
        Cart saveCart = cartService.saveCart(pid, uid);
        if(ObjectUtils.isEmpty(saveCart)){
            session.setAttribute("errorMsg", "Lỗi !!! Không thể thêm sản phẩm vào giỏ hàng !");
        } else {
            session.setAttribute("succMsg", "Đã thêm vào giỏ hàng !");
        }
        return "redirect:/viewProduct/"+pid;
    }

    //Load cart.html
    @GetMapping("/cart")
    public String loadCart(Principal p, Model model){
        // Lấy thông tin người dùng đã đăng nhập
        UserDtls user = getLoggedInUserDetails(p);

        // Lấy danh sách giỏ hàng của người dùng
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        model.addAttribute("carts", carts);

        if (carts.size() > 0) {
            Double totalProductPrice = 0.0;

            // Duyệt qua từng sản phẩm trong giỏ hàng
            for (Cart cartItem : carts) {
                Product product = cartItem.getProduct();
                Double productPrice = product.getFinalPrice(); // Giá niêm yết của sản phẩm

                // Kiểm tra nếu sản phẩm có giảm giá
                if (product.getDiscount() != null && product.getDiscount() > 0) {
                    // Tính giá sau khi giảm
                    productPrice = productPrice * (1 - product.getDiscount() / 100);
                }

                // Cộng dồn vào tổng giá sản phẩm, nhân với số lượng
                totalProductPrice += productPrice * cartItem.getQuantity();
            }

            // Thêm tổng giá vào model
            model.addAttribute("totalProductPrice", totalProductPrice);
        }

        // Trả về trang giỏ hàng
        return "user/cart";
    }

    @GetMapping("/cartQuantityUpdate")
    public String cartQuantityUpdate(@RequestParam String sy, @RequestParam int cid, HttpSession session) {
        commonService.removeSessionMessage();
        try {
            cartService.updateQuantity(sy, cid);
            if (sy.equalsIgnoreCase("de")) {
                //session.setAttribute("succMsg", "Sản phẩm đã được xóa khỏi giỏ hàng.");
            } else {
                session.setAttribute("succMsg", "Thêm vào giỏ hàng.");
            }
        } catch (RuntimeException e) {
            session.setAttribute("errorMsg", e.getMessage()); // Lấy thông điệp lỗi từ ngoại lệ
        }
        return "redirect:/user/cart";
    }
    
    private UserDtls getLoggedInUserDetails(Principal p) {
        String email = p.getName();
        UserDtls userDtls = userService.getUserByEmail(email);

        return userDtls;
    }

    // Lấy trang giỏ hàng và tính toán giá trị
    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam String code,
                              @RequestParam Double orderAmount,
                              Model model,
                              HttpSession session,  // Sử dụng HttpSession
                              Principal principal) {
        UserDtls user = getLoggedInUserDetails(principal);
        try {
            Double discount = couponService.applyCoupon(user, code, orderAmount);
            session.setAttribute("discount", discount);  // Lưu discount vào session
            session.setAttribute("couponCode", code);  // Lưu mã coupon vào session
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "redirect:/user/orders";  // Redirect lại về trang giỏ hàng
    }

    @PostMapping("/remove-coupon")
    public String removeCoupon(@RequestParam String code, Principal principal, Model model) {
        UserDtls user = getLoggedInUserDetails(principal);
        try {
            couponService.removeCoupon(user, code);
            model.addAttribute("message", "Mã giảm giá đã được xóa.");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "redirect:/user/orders";
    }
    
    @GetMapping("/orders")
    public String orderPage(Model model, Principal p, HttpSession session) {
        UserDtls user = getLoggedInUserDetails(p);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        List<Address> addresses = addressService.getAddressesByUser(user.getId());
        model.addAttribute("carts", carts);
        model.addAttribute("addresses", addresses);
    
        Double discount = (Double) session.getAttribute("discount");  // Lấy discount từ session
        String couponCode = (String) session.getAttribute("couponCode");  // Lấy couponCode từ session
    
        if (!carts.isEmpty()) {
            Double orderPrice = carts.get(carts.size() - 1).getTotalProductPrice();
            Double deliveryPrice = Math.min(orderPrice * 0.01, 15000.0);
    
            model.addAttribute("totalPrice", orderPrice);
            model.addAttribute("deliveryPrice", deliveryPrice);
            BigDecimal totalOrderPrice = new BigDecimal(orderPrice + deliveryPrice - (discount != null ? discount : 0));
            totalOrderPrice = totalOrderPrice.setScale(2, RoundingMode.HALF_UP);  // Làm tròn lên 2 chữ số thập phân

            model.addAttribute("totalOrderPrice", totalOrderPrice.doubleValue());
            model.addAttribute("discount", discount);
            model.addAttribute("couponCode", couponCode);  // Truyền couponCode vào view
    
        } else {
            model.addAttribute("message", "Giỏ hàng trống!");
        }
    
        return "user/order";
    }

    // Lưu đơn hàng mới
    @PostMapping("/save-order")
    public String saveOrder(@ModelAttribute OrderRequest orderRequest, Principal p) {
        UserDtls user = getLoggedInUserDetails(p);
        orderService.saveOrder(user.getId(), orderRequest);
        return "redirect:/user/success";
    }    

    // Trang xác nhận thành công
    @GetMapping("/success")
    public String loadSuccess() {
        return "user/success";
    }

    // Tải danh sách đơn hàng của người dùng
    @GetMapping("/myOrder")
    public String loadMyOrder(Model model, Principal p) {
        UserDtls loginUser = getLoggedInUserDetails(p);
        List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
        model.addAttribute("orders", orders);
        return "user/my_order";
    }

    @GetMapping("/order/{id}")
    public String getOrderDetails(@PathVariable("id") int orderId, Model model) {
        try {
            ProductOrder order = orderService.getOrderById(orderId); // Lấy đơn hàng theo ID
            model.addAttribute("order", order); // Thêm đơn hàng vào mô hình
            return "user/order_details"; // Trả về trang chi tiết đơn hàng
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage()); // Thêm thông báo lỗi vào mô hình
            return "user/error"; // Trả về trang lỗi hoặc trang khác
        }
    }    

    // Cập nhật trạng thái đơn hàng
    @SuppressWarnings("null")
    @GetMapping("/update-status")
    public String updateOrderStatus(@RequestParam int id, @RequestParam String st, HttpSession session){
        
        // Lấy tên trạng thái từ enum
        OrderEnum[] values = OrderEnum.values();
        String status = null;
    
        for(OrderEnum orderSt : values){
            if(orderSt.getId() == Integer.parseInt(st)){
                status = orderSt.getName();
                break;
            }
        }
    
        ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
        commonUtil.sendOrderMail(updateOrder, status);

        if (!ObjectUtils.isEmpty(updateOrder)) {
            if (status.equals("Hủy")) {
                // Cập nhật số lượng tồn kho khi hủy đơn
                orderService.handleStockAfterCancellation(updateOrder);
            }
            session.setAttribute("succMsg", "Đã hủy đơn hàng !");
        } else {
            session.setAttribute("errorMsg", "Không thể hủy đơn hàng !");
        }
    
        return "redirect:/user/myOrder";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        UserDtls user = getLoggedInUserDetails(principal);
        List<Address> addresses = addressService.getAddressesByUser(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        return "user/profile";
    }

    // Hiển thị trang thêm địa chỉ
    @GetMapping("/add-address")
    public String showAddAddressForm(Model model) {
        model.addAttribute("addressRequest", new AddressRequest());
        return "user/add-address";  // trang tạo địa chỉ
    }

    // Xử lý thêm địa chỉ
    @PostMapping("/add-address")
    public String addAddress(@ModelAttribute AddressRequest addressRequest, Principal principal, RedirectAttributes redirectAttributes) {
        UserDtls user = getLoggedInUserDetails(principal);
        addressService.createAddress(addressRequest, user.getId());
        redirectAttributes.addFlashAttribute("succMsg", "Địa chỉ đã được thêm thành công!");
        return "redirect:/user/add-address";
    }

    // Xóa địa chỉ
    @PostMapping("/delete-address/{id}")
    public String deleteAddress(@PathVariable UUID id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            UserDtls user = getLoggedInUserDetails(principal);

            addressService.deleteAddress(id, user.getId());
            redirectAttributes.addFlashAttribute("succMsg", "Địa chỉ đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể xóa địa chỉ: " + e.getMessage());
        }
        return "redirect:/user/profile";  // Quay lại trang profile sau khi xóa
    }

    // Sửa địa chỉ
    @GetMapping("/edit-address/{id}")
    public String showEditAddressForm(@PathVariable UUID id, Model model, Principal principal) {
        UserDtls user = getLoggedInUserDetails(principal);
        Address address = addressService.getAddressById(id, user.getId());
        model.addAttribute("address", address);
        return "user/edit-address";  // Trang chỉnh sửa địa chỉ
    }

    @PostMapping("/updateAddress")
    public String editAddress(@RequestParam UUID id, @ModelAttribute AddressRequest addressRequest, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            UserDtls user = getLoggedInUserDetails(principal);
            addressService.editAddress(id, addressRequest, user.getId());
            redirectAttributes.addFlashAttribute("succMsg", "Địa chỉ đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể cập nhật địa chỉ: " + e.getMessage());
        }
        return "redirect:/user/profile";  // Quay lại trang profile sau khi sửa
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute UserDtls user ,@RequestParam MultipartFile img){
        userService.updateProfile(user, img);
        return "redirect:/user/profile";
    }
    
    @PostMapping("/update-password")
    public String updatePassword(
        @RequestParam String newPassword,
        @RequestParam String confirmPassword,
        @RequestParam String currentPassword,
        Principal p,
        HttpSession session) {

        // Lấy thông tin người dùng hiện tại
        UserDtls loggedInUserDtls = getLoggedInUserDetails(p);

        // Kiểm tra mật khẩu hiện tại
        boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDtls.getPassword());

        if (matches) {
            // Kiểm tra xác nhận mật khẩu mới
            if (newPassword.equals(confirmPassword)) {
                // Mã hóa và lưu mật khẩu mới
                String encodedPassword = passwordEncoder.encode(newPassword);
                loggedInUserDtls.setPassword(encodedPassword);
                UserDtls savedUser = userService.updateUser(loggedInUserDtls);
            
                if (ObjectUtils.isEmpty(savedUser)) {
                    session.setAttribute("errorMsg", "Lỗi server !!! Không cập nhập được mật khẩu");
                } else {
                    session.setAttribute("succMsg", "Đổi mật khẩu thành công!!!");
            }
            } else {
                session.setAttribute("errorMsg", "Mật khẩu mới và mật khẩu xác nhận không khớp!");
            }
        } else {
            session.setAttribute("errorMsg", "Mật khẩu hiện tại không đúng!");
        }

        return "redirect:/user/profile";
    }

}