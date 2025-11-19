package com.capstone.capstone.dto.response.warehouseItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateWarehouseItemResponse {
    private UUID warehouseItemId;
    private String itemName;
    private String itemUnit;
    private int quantity;
}
