package com.capstone.capstone.dto.response.warehouseItem;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateWarehouseItemResponse {
    private UUID warehouseItemId;
    private String itemName;
    private String itemUnit;
    private int quantity;
}
