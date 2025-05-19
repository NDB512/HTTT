package com.example.shopping.services;

import java.util.List;

import com.example.shopping.models.Supplier;

public interface SupplierService {
    public List<Supplier> getAllSuppliers();
    public Supplier saveSupplier(Supplier supplier);
    public Supplier findById(int id);
    Supplier updateSupplier(Supplier supplier);
    void deleteSupplier(int id);
}
