package com.example.shopping.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.shopping.utils.WarehouseReceiptType;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class WarehouseReceiptForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String warehouseReceiptId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseReceiptType type;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(length = 500)
    private String note;
    
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "warehouse_receipt_id")
    private List<WarehouseReceiptItem> items = new ArrayList<>();

    @Transient
    private String formattedDate;

}
