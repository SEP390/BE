package com.capstone.capstone.repository;

import com.capstone.capstone.entity.WarehouseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WarehouseTransactionRepository extends JpaRepository<WarehouseTransaction, UUID> {
}
