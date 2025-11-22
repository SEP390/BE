package com.capstone.capstone.repository;

import com.capstone.capstone.entity.User;
import com.capstone.capstone.entity.WarehouseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WarehouseTransactionRepository extends JpaRepository<WarehouseTransaction, UUID> {
    List<WarehouseTransaction> findAllByUser(User user);
}
