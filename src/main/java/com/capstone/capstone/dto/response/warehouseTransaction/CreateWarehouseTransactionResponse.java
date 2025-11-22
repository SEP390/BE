package com.capstone.capstone.dto.response.warehouseTransaction;

import com.capstone.capstone.dto.enums.TransactionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateWarehouseTransactionResponse {
    private UUID id;
    private UUID requestId;
    private UUID reportId;
    private UUID actionById;
    private String actionByName;
    private LocalDateTime createdAt;
    private TransactionTypeEnum transactionType;
    private int transactionQuantity;
    private String note;
}
