package com.capstone.capstone.repository;

import com.capstone.capstone.entity.WarehouseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WarehouseItemRepository extends JpaRepository<WarehouseItem, UUID> {
}
