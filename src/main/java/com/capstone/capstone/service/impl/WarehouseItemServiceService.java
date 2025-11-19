package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.warehouseItem.CreateWarehouseItemRequest;
import com.capstone.capstone.dto.request.warehouseItem.UpdateWarehouseItemRequest;
import com.capstone.capstone.dto.response.warehouseItem.CreateWarehouseItemResponse;
import com.capstone.capstone.dto.response.warehouseItem.GetAllWarehouseItemResponse;
import com.capstone.capstone.dto.response.warehouseItem.UpdateWarehouseItemResponse;
import com.capstone.capstone.entity.WarehouseItem;
import com.capstone.capstone.repository.WarehouseItemRepository;
import com.capstone.capstone.service.interfaces.IWarehouseItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseItemServiceService implements IWarehouseItemService {

    private final WarehouseItemRepository warehouseItemRepository;

    @Override
    public CreateWarehouseItemResponse createWarehouseItem(CreateWarehouseItemRequest createWarehouseItemRequest) {
        WarehouseItem warehouseItem = new WarehouseItem();
        warehouseItem.setItemName(createWarehouseItemRequest.getItemName());
        warehouseItem.setUnit(createWarehouseItemRequest.getItemUnit());
        warehouseItem.setQuantity(0);
        warehouseItemRepository.save(warehouseItem);
        CreateWarehouseItemResponse response = new CreateWarehouseItemResponse();
        response.setWarehouseItemId(warehouseItem.getId());
        response.setItemName(warehouseItem.getItemName());
        response.setItemUnit(warehouseItem.getUnit());
        response.setQuantity(warehouseItem.getQuantity());
        return response;
    }

    @Override
    public List<GetAllWarehouseItemResponse> getAllWarehouseItem() {
        List<WarehouseItem> warehouseItems = warehouseItemRepository.findAll();
        List<GetAllWarehouseItemResponse> responseList = new ArrayList<>();
        for (WarehouseItem warehouseItem : warehouseItems) {
            GetAllWarehouseItemResponse response = new GetAllWarehouseItemResponse();
            response.setWarehouseItemId(warehouseItem.getId());
            response.setItemName(warehouseItem.getItemName());
            response.setItemUnit(warehouseItem.getUnit());
            response.setQuantity(warehouseItem.getQuantity());
            responseList.add(response);
        }
        return responseList;
    }

    @Override
    public UpdateWarehouseItemResponse updateWarehouseItem(UUID wareHouseId, UpdateWarehouseItemRequest updateWarehouseItemRequest) {
        WarehouseItem warehouseItem = warehouseItemRepository.findById(wareHouseId).get();
        warehouseItem.setItemName(updateWarehouseItemRequest.getItemName());
        warehouseItem.setUnit(updateWarehouseItemRequest.getItemUnit());
        warehouseItemRepository.save(warehouseItem);
        UpdateWarehouseItemResponse response = new UpdateWarehouseItemResponse();
        response.setWarehouseItemId(warehouseItem.getId());
        response.setItemName(warehouseItem.getItemName());
        response.setItemUnit(warehouseItem.getUnit());
        response.setQuantity(warehouseItem.getQuantity());
        return response;
    }
}
