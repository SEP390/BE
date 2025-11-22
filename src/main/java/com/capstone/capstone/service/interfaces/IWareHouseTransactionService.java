package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.warehouseTransaction.CreateWarehouseTransactionRequest;
import com.capstone.capstone.dto.response.warehouseTransaction.CreateWarehouseTransactionResponse;

public interface IWareHouseTransactionService {
    CreateWarehouseTransactionResponse createWarehouseTransaction(CreateWarehouseTransactionRequest request);
}
