package com.capstone.capstone.dto.request.warehouseTransaction;

import com.capstone.capstone.dto.enums.TransactionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateWarehouseTransactionRequest {
    private UUID itemId;
    private UUID reportId;
    private UUID requestId;
    private TransactionTypeEnum transactionType;
    private int transactionQuantity;
    private String note;
}
