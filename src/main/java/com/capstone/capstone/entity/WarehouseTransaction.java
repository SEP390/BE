package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.TransactionTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseTransaction extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private WarehouseItem item; // Sản phẩm liên quan

    @ManyToOne
    @JoinColumn(name = "action_by", nullable = false)
    private User user; // Ai thực hiện (TECHNICAL)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionTypeEnum type; // IMPORT / EXPORT

    @Column(nullable = false)
    private Integer quantity; // Số lượng thay đổi

    private String note; // Note thêm

    private UUID requestId; // Nếu phục vụ request
    private UUID reportId;  // Nếu phục vụ report

    // Nếu BaseEntity đã có createdAt rồi thì xóa field này đi.
    @Column(nullable = false)
    private LocalDateTime createdAt;
}