package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.warehouseItem.CreateWarehouseItemRequest;
import com.capstone.capstone.dto.request.warehouseItem.UpdateWarehouseItemRequest;
import com.capstone.capstone.dto.response.warehouseItem.CreateWarehouseItemResponse;
import com.capstone.capstone.dto.response.warehouseItem.GetAllWarehouseItemResponse;
import com.capstone.capstone.dto.response.warehouseItem.UpdateWarehouseItemResponse;

import java.util.List;
import java.util.UUID;

public interface IWarehouseItemService {
    CreateWarehouseItemResponse createWarehouseItem(CreateWarehouseItemRequest createWarehouseItemRequest);
    List<GetAllWarehouseItemResponse> getAllWarehouseItem();
    UpdateWarehouseItemResponse updateWarehouseItem(UUID wareHouseId, UpdateWarehouseItemRequest updateWarehouseItemRequest);
}
