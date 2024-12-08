package com.example.shopping.services.impls;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.shopping.models.Product;
import com.example.shopping.models.WarehouseReceiptForm;
import com.example.shopping.models.WarehouseReceiptItem;
import com.example.shopping.repositories.ProductRepository;
import com.example.shopping.repositories.WarehouseReceiptRepository;
import com.example.shopping.services.WarehouseReceiptService;
import com.example.shopping.utils.WarehouseReceiptType;

@Service
public class WarehouseReceiptServiceImpl implements WarehouseReceiptService{

    @Autowired
    private WarehouseReceiptRepository warehouseReceiptRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<WarehouseReceiptForm> allWarehouseReceiptForm() {
        return warehouseReceiptRepository.findAll();
    }

    private String createWarehouseReceiptId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuidPart = UUID.randomUUID().toString().substring(0, 8); // Lấy 8 ký tự đầu của UUID
        return "WR-" + datePart + "-" + uuidPart;
    }

    @Override
    public WarehouseReceiptForm saveWarehouseReceiptForm(WarehouseReceiptForm warehouseReceiptForm) {
        // Tạo mã tự động cho warehouseReceiptId
        String generatedId = createWarehouseReceiptId();
        warehouseReceiptForm.setWarehouseReceiptId(generatedId);
    
        // Xử lý số lượng tồn kho dựa trên loại phiếu
        for (WarehouseReceiptItem item : warehouseReceiptForm.getItems()) {
            // Tìm sản phẩm trong cơ sở dữ liệu dựa trên ID
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
    
            if (warehouseReceiptForm.getType() == WarehouseReceiptType.IMPORT) {
                // Cộng số lượng tồn kho nếu là phiếu nhập
                product.setStock(product.getStock() + item.getQuantity());
            } else if (warehouseReceiptForm.getType() == WarehouseReceiptType.EXPORT) {
                // Kiểm tra số lượng tồn kho trước khi trừ
                if (product.getStock() >= item.getQuantity()) {
                    product.setStock(product.getStock() - item.getQuantity());
                } else {
                    throw new RuntimeException("Số lượng yêu cầu lớn hơn số lượng trong kho");
                }
            }
    
            // Lưu lại sản phẩm đã cập nhật
            productRepository.save(product);
        }
    
        // Lưu phiếu nhập kho
        return warehouseReceiptRepository.save(warehouseReceiptForm);
    }

    @Override
    public Page<WarehouseReceiptForm> getAllWarehouseReceiptFormPage(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return warehouseReceiptRepository.findAll(pageable);
    }

    @Override
    public Page<WarehouseReceiptForm> getSeacrhWarehouseReceiptForm(int pageNo, int pageSize, String ch) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return warehouseReceiptRepository.findByWarehouseReceiptId(ch, ch, pageable);
    }
    
}