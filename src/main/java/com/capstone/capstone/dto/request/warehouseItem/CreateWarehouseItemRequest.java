package com.capstone.capstone.dto.request.warehouseItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateWarehouseItemRequest {
    private String itemName;
    private String itemUnit;
}
