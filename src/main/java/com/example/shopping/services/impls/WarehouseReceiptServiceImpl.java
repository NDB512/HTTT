package com.example.shopping.services.impls;

import com.example.shopping.models.Product;
import com.example.shopping.models.Supplier;
import com.example.shopping.models.WarehouseReceiptForm;
import com.example.shopping.models.WarehouseReceiptItem;
import com.example.shopping.repositories.ProductRepository;
import com.example.shopping.repositories.SupplierRepository;
import com.example.shopping.repositories.WarehouseReceiptRepository;
import com.example.shopping.services.WarehouseReceiptService;
import com.example.shopping.utils.WarehouseReceiptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class WarehouseReceiptServiceImpl implements WarehouseReceiptService {

    @Autowired
    private WarehouseReceiptRepository warehouseReceiptRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public List<WarehouseReceiptForm> allWarehouseReceiptForm() {
        return warehouseReceiptRepository.findAll();
    }

    private String createWarehouseReceiptId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);
        return "WR-" + datePart + "-" + uuidPart;
    }

    @Override
    public WarehouseReceiptForm saveWarehouseReceiptForm(WarehouseReceiptForm warehouseReceiptForm) {
        // Tạo mã tự động cho warehouseReceiptId
        String generatedId = createWarehouseReceiptId();
        warehouseReceiptForm.setWarehouseReceiptId(generatedId);

        // Kiểm tra nhà cung ứng cho phiếu nhập
        if (warehouseReceiptForm.getType() == WarehouseReceiptType.IMPORT) {
            if (warehouseReceiptForm.getSupplier() == null || warehouseReceiptForm.getSupplier().getId() == 0) {
                throw new RuntimeException("Phiếu nhập kho phải có thông tin nhà cung ứng");
            }
            Supplier supplier = supplierRepository.findById(warehouseReceiptForm.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Nhà cung ứng không tồn tại"));
            warehouseReceiptForm.setSupplier(supplier);
        } else {
            warehouseReceiptForm.setSupplier(null); // Phiếu xuất không cần nhà cung ứng
        }

        // Xử lý số lượng tồn kho
        for (WarehouseReceiptItem item : warehouseReceiptForm.getItems()) {
            // Tìm sản phẩm trong cơ sở dữ liệu dựa trên ID
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

            if (warehouseReceiptForm.getType() == WarehouseReceiptType.IMPORT) {
                // Cộng số lượng tồn kho nếu là phiếu nhập
                product.setStock(product.getStock() + item.getQuantity());
            } else if (warehouseReceiptForm.getType() == WarehouseReceiptType.EXPORT) {
                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Số lượng yêu cầu lớn hơn số lượng trong kho");
                }
                product.setStock(product.getStock() - item.getQuantity());
            }
    
            // Lưu lại sản phẩm đã cập nhật
            productRepository.save(product);
        }

        warehouseReceiptForm.setDate(LocalDateTime.now());

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