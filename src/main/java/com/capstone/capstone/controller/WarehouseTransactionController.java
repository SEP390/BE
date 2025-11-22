package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.warehouseTransaction.CreateWarehouseTransactionRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.warehouseTransaction.CreateWarehouseTransactionResponse;
import com.capstone.capstone.service.interfaces.IWareHouseTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.WAREHOUSE_TRANSACTION.WAREHOUSE_TRANSACTION)
public class WarehouseTransactionController {
    private final IWareHouseTransactionService wareHouseTransactionService;

    @PostMapping
    public ResponseEntity<BaseResponse<CreateWarehouseTransactionResponse>> createWarehouseTransaction(@RequestBody CreateWarehouseTransactionRequest request) {
        BaseResponse<CreateWarehouseTransactionResponse> response = new BaseResponse<>();
        response.setData(wareHouseTransactionService.createWarehouseTransaction(request));
        response.setMessage("Warehouse Transaction Created");
        response.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
