package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.enums.TransactionTypeEnum;
import com.capstone.capstone.dto.request.warehouseTransaction.CreateWarehouseTransactionRequest;
import com.capstone.capstone.dto.response.warehouseTransaction.CreateWarehouseTransactionResponse;
import com.capstone.capstone.dto.response.warehouseTransaction.GetAllWarehouseTransactionResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.entity.WarehouseItem;
import com.capstone.capstone.entity.WarehouseTransaction;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.repository.WarehouseItemRepository;
import com.capstone.capstone.repository.WarehouseTransactionRepository;
import com.capstone.capstone.service.interfaces.IWareHouseTransactionService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class WareHouseTransactionService implements IWareHouseTransactionService {
    private final WarehouseTransactionRepository  warehouseTransactionRepository;
    private final UserRepository userRepository;
    private final WarehouseItemRepository warehouseItemRepository;
    @Override
    public CreateWarehouseTransactionResponse createWarehouseTransaction(CreateWarehouseTransactionRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        WarehouseItem item = warehouseItemRepository.findById(request.getItemId()).orElseThrow(()-> new RuntimeException("Item not found"));
        WarehouseTransaction transaction = new WarehouseTransaction();
        if(user.getRole() == RoleEnum.TECHNICAL){
            transaction.setUser(user);
        } else {
            throw new RuntimeException("Role not allowed");
        }
        transaction.setItem(item);
        transaction.setType(request.getTransactionType());
        transaction.setQuantity(request.getTransactionQuantity());
        transaction.setNote(request.getNote());
        transaction.setReportId(request.getReportId());
        transaction.setRequestId(request.getRequestId());
        transaction.setCreatedAt(LocalDateTime.now());
        if(request.getTransactionType() == TransactionTypeEnum.IMPORT){
            item.setQuantity(item.getQuantity() + transaction.getQuantity());
        } else if (request.getTransactionType() == TransactionTypeEnum.EXPORT) {
            item.setQuantity(item.getQuantity() - transaction.getQuantity());
        }
        warehouseTransactionRepository.save(transaction);
        CreateWarehouseTransactionResponse response = new CreateWarehouseTransactionResponse();
        response.setId(transaction.getId());
        response.setRequestId(transaction.getRequestId());
        response.setReportId(transaction.getReportId());
        response.setActionById(transaction.getUser().getId());
        response.setActionByName(transaction.getUser().getFullName());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setTransactionType(transaction.getType());
        response.setTransactionQuantity(transaction.getQuantity());
        response.setNote(transaction.getNote());
        return  response;
    }

    @Override
    public List<GetAllWarehouseTransactionResponse> getAllTransactions() {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        List<WarehouseTransaction> warehouseTransactions = new ArrayList<>();
        if(user.getRole() == RoleEnum.TECHNICAL){
            warehouseTransactions = warehouseTransactionRepository.findAllByUser(user);
        }  else if(user.getRole() == RoleEnum.MANAGER){
            warehouseTransactions = warehouseTransactionRepository.findAll();
        }
        List<GetAllWarehouseTransactionResponse> response = new ArrayList<>();
        for (WarehouseTransaction transaction : warehouseTransactions) {
            GetAllWarehouseTransactionResponse  res = new GetAllWarehouseTransactionResponse();
            res.setId(transaction.getId());
            res.setRequestId(transaction.getRequestId());
            res.setReportId(transaction.getReportId());
            res.setActionById(transaction.getUser().getId());
            res.setActionByName(transaction.getUser().getFullName());
            res.setCreatedAt(transaction.getCreatedAt());
            res.setTransactionType(transaction.getType());
            res.setTransactionQuantity(transaction.getQuantity());
            res.setNote(transaction.getNote());
            response.add(res);
        }
        return response;
    }
}
