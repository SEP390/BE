package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.warehouseItem.CreateWarehouseItemRequest;
import com.capstone.capstone.dto.request.warehouseItem.UpdateWarehouseItemRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.warehouseItem.CreateWarehouseItemResponse;
import com.capstone.capstone.dto.response.warehouseItem.GetAllWarehouseItemResponse;
import com.capstone.capstone.dto.response.warehouseItem.UpdateWarehouseItemResponse;
import com.capstone.capstone.service.interfaces.IWarehouseItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.WAREHOUSE_ITEM.WAREHOUSE_ITEM)
public class WarehouseItemController {
    private final IWarehouseItemService warehouseItemService;

    @PostMapping
    public ResponseEntity<BaseResponse<CreateWarehouseItemResponse>> createWarehouseItem(@RequestBody CreateWarehouseItemRequest request){
        BaseResponse<CreateWarehouseItemResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(warehouseItemService.createWarehouseItem(request));
        baseResponse.setMessage("Warehouse Item Created");
        baseResponse.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<GetAllWarehouseItemResponse>>> getAllWarehouseItem(){
        BaseResponse<List<GetAllWarehouseItemResponse>> baseResponse = new BaseResponse<>();
        baseResponse.setData(warehouseItemService.getAllWarehouseItem());
        baseResponse.setMessage("Warehouse Item List");
        baseResponse.setStatus(HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }

    @PutMapping(ApiConstant.WAREHOUSE_ITEM.GET_BY_ID)
    public ResponseEntity<BaseResponse<UpdateWarehouseItemResponse>> updateWarehouseItem(@PathVariable UUID id , @RequestBody UpdateWarehouseItemRequest request){
        UpdateWarehouseItemResponse updateWarehouseItemResponse = warehouseItemService.updateWarehouseItem(id, request);
        BaseResponse<UpdateWarehouseItemResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(updateWarehouseItemResponse);
        baseResponse.setMessage("Warehouse Item Updated");
        baseResponse.setStatus(HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }
}

