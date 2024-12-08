package com.example.shopping.services.impls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.shopping.dto.SalesReportDto;
import com.example.shopping.models.*;
import com.example.shopping.repositories.AddressRepository;
import com.example.shopping.repositories.CartRepository;
import com.example.shopping.repositories.ProductOrderRepository;
import com.example.shopping.repositories.ProductRepository;
import com.example.shopping.repositories.UserRepository;
import com.example.shopping.services.OrderService;
import com.example.shopping.utils.CommonUtil;
import com.example.shopping.utils.OrderEnum;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private CartRepository cartRepository;  

    @Autowired 
    private UserRepository userRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public void saveOrder(int userId, OrderRequest orderRequest) {
        // Lấy giỏ hàng của người dùng
        List<Cart> carts = cartRepository.getCartsByUser(userId);
        UserDtls userDtls = userRepository.findById(userId).get();

        // Lấy địa chỉ đã chọn
        Address selectedAddress = addressRepository.findById(orderRequest.getAddressId())
        .orElseThrow(() -> new RuntimeException("Address not found"));
    
        // Tạo đơn hàng
        ProductOrder productOrder = new ProductOrder();
        productOrder.setOrderId(UUID.randomUUID().toString());
        productOrder.setOrderedDay(LocalDateTime.now());
        productOrder.setStatus(OrderEnum.IN_PROGRESS.getName());
        productOrder.setPaymentType(orderRequest.getPaymentType());
        productOrder.setUser(userDtls);
        productOrder.setAddress(selectedAddress);
    
        // Thiết lập tổng giá đơn hàng và phí vận chuyển
        productOrder.setDeliveryPrice(orderRequest.getDeliveryPrice());

        productOrder.setDiscount(orderRequest.getDiscount());

        // Tạo danh sách các sản phẩm trong đơn hàng
        List<OrderItem> items = new ArrayList<>();

        for (Cart c : carts) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(c.getProduct());
            orderItem.setQuantity(c.getQuantity());
    
            // Tính giá sau giảm nếu có
            Double productPrice = c.getProduct().getFinalPrice();
            if (c.getProduct().getDiscount() != null && c.getProduct().getDiscount() > 0) {
                productPrice = productPrice * (1 - c.getProduct().getDiscount() / 100);
            }
    
            // Cập nhật giá cho sản phẩm trong đơn hàng
            orderItem.setPrice(productPrice);
    
            items.add(orderItem);
        }
    
        // Cập nhật tổng giá đơn hàng
        productOrder.setTotalOrderPrice(orderRequest.getTotalOrderPrice());
    
        productOrder.setItems(items); // Thêm danh sách sản phẩm vào đơn hàng
    
        // Lưu đơn hàng vào cơ sở dữ liệu
        ProductOrder save = productOrderRepository.save(productOrder);
    
        // Gửi email thông báo đơn hàng
        commonUtil.sendOrderMail(save, "Đang xử lý");
    
        // Xóa các sản phẩm trong giỏ hàng sau khi đặt hàng
        cartRepository.deleteAll(carts);
    }

    @Override
    public List<ProductOrder> getOrdersByUser(int id) {
        return productOrderRepository.findByUserId(id);
    }

    @Override
    public ProductOrder updateOrderStatus(int id, String st) {
        // Kiểm tra trạng thái có hợp lệ không
        if (st == null || st.isEmpty()) {
            return null; // Trả về null nếu trạng thái không hợp lệ
        }
    
        // Tìm đơn hàng theo id
        Optional<ProductOrder> order = productOrderRepository.findById(id);
        if (order.isPresent()) {
            // Giữ lại đối tượng ProductOrder
            ProductOrder productOrder = order.get();
            // Cập nhật trạng thái cho đơn hàng
            productOrder.setStatus(st);
            productOrderRepository.save(productOrder); // Lưu đơn hàng sau khi cập nhật
            return productOrder; // Trả về đối tượng ProductOrder đã cập nhật
        }
    
        System.out.println("Order not found with id: " + id);
        return null; // Trả về null nếu không tìm thấy đơn hàng
    }    

    @Override
    public void handleStockAfterCancellation(ProductOrder order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int quantityToReturn = item.getQuantity();
            product.setStock(product.getStock() + quantityToReturn); // Trả lại số lượng sản phẩm vào kho
            productRepository.save(product); // Lưu cập nhật sản phẩm
        }
    }

    @Override
    public ProductOrder getOrderById(int orderId) {
        return productOrderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại.")); // Ném ra ngoại lệ nếu không tìm thấy
    }

    @Override
    public List<ProductOrder> getAllOrders() {
        return productOrderRepository.findAll();
    }

    @Override
    public Page<ProductOrder> getAllOrdersPage(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productOrderRepository.findAll(pageable);
    }

    @Override
    public ProductOrder getOrderByOrderid(String orderId) {
        return productOrderRepository.findByOrderId(orderId);
    }

    @Override
    public List<SalesReportDto> generateSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<ProductOrder> orders = productOrderRepository.findOrdersWithinDateRange(startDate, endDate);
    
        Map<String, SalesReportDto> reportMap = new HashMap<>();
        for (ProductOrder order : orders) {
            // Định dạng ngày làm khóa (có thể là yyyy-MM-dd, yyyy-MM, hoặc yyyy)
            String reportKey = order.getOrderedDay().toLocalDate().toString();
    
            // Tạo mới hoặc cập nhật báo cáo
            SalesReportDto dto = reportMap.getOrDefault(reportKey, new SalesReportDto(reportKey, 0.0, 0));
            dto.setTotalRevenue(dto.getTotalRevenue() + order.getTotalOrderPrice());
            dto.setTotalOrders(dto.getTotalOrders() + 1);
    
            reportMap.put(reportKey, dto);
        }
    
        return new ArrayList<>(reportMap.values());
    }
    
}
