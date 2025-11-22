package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.warehouseTransaction.CreateWarehouseTransactionRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.warehouseTransaction.CreateWarehouseTransactionResponse;
import com.capstone.capstone.dto.response.warehouseTransaction.GetAllWarehouseTransactionResponse;
import com.capstone.capstone.service.interfaces.IWareHouseTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    @GetMapping
    public ResponseEntity<BaseResponse<List<GetAllWarehouseTransactionResponse>>>  getAllWarehouseTransaction() {
        BaseResponse<List<GetAllWarehouseTransactionResponse>> response = new BaseResponse<>();
        List<GetAllWarehouseTransactionResponse> responseList = wareHouseTransactionService.getAllTransactions();
        response.setData(responseList);
        response.setMessage("Warehouse Transaction List");
        response.setStatus(HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
