package com.example.shopping.services.impls;

import com.example.shopping.models.Supplier;
import com.example.shopping.repositories.SupplierRepository;
import com.example.shopping.repositories.WarehouseReceiptRepository;
import com.example.shopping.services.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private WarehouseReceiptRepository warehouseReceiptRepository;

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    public Supplier saveSupplier(Supplier supplier) {
        if (supplier.getSupplierCode() == null || supplier.getSupplierCode().isEmpty()) {
            supplier.setSupplierCode("SUP-" + UUID.randomUUID().toString().substring(0, 8));
        }
        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier findById(int id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhà cung ứng không tồn tại"));
    }

    @Override
    public Supplier updateSupplier(Supplier supplier) {
        Supplier existingSupplier = findById(supplier.getId());
        existingSupplier.setSupplierCode(supplier.getSupplierCode());
        existingSupplier.setName(supplier.getName());
        existingSupplier.setAddress(supplier.getAddress());
        existingSupplier.setPhone(supplier.getPhone());
        existingSupplier.setEmail(supplier.getEmail());
        return supplierRepository.save(existingSupplier);
    }

    @Override
    public void deleteSupplier(int id) {
        if (warehouseReceiptRepository.existsBySupplier_Id(id)) {
            throw new RuntimeException("Không thể xóa nhà cung ứng vì đang được sử dụng trong phiếu kho");
        }
        Supplier supplier = findById(id);
        supplierRepository.delete(supplier);
    }
}