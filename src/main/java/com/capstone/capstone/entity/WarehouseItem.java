package com.capstone.capstone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WarehouseItem extends BaseEntity {

    @Column(nullable = false)
    private String itemName; // Tên sản phẩm

    @Column(nullable = false)
    private Integer quantity; // Số lượng hiện tại trong kho

    @Column(nullable = false)
    private String unit; // Đơn vị tính (chai, cái, bộ,...)

    // Lịch sử import/export của item này
    @OneToMany(mappedBy = "item")
    private List<WarehouseTransaction> transactions = new ArrayList<>();
}