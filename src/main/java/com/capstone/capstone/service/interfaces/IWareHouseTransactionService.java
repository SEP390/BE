package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.warehouseTransaction.CreateWarehouseTransactionRequest;
import com.capstone.capstone.dto.response.warehouseTransaction.CreateWarehouseTransactionResponse;
import com.capstone.capstone.dto.response.warehouseTransaction.GetAllWarehouseTransactionResponse;

import java.util.List;

public interface IWareHouseTransactionService {
    CreateWarehouseTransactionResponse createWarehouseTransaction(CreateWarehouseTransactionRequest request);
    List<GetAllWarehouseTransactionResponse> getAllTransactions();

}
