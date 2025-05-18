package com.example.shopping.services;

import java.util.List;
import org.springframework.data.domain.Page;


import com.example.shopping.models.WarehouseReceiptForm;

public interface WarehouseReceiptService {
    public WarehouseReceiptForm saveWarehouseReceiptForm(WarehouseReceiptForm warehouseReceiptForm);
    
    List<WarehouseReceiptForm> allWarehouseReceiptForm();

    Page<WarehouseReceiptForm> getAllWarehouseReceiptFormPage(int pageNo, int pageSize);

    Page<WarehouseReceiptForm> getSeacrhWarehouseReceiptForm(int pageNo, int pageSize, String ch);
}
