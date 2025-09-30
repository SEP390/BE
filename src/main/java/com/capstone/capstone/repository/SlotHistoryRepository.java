package com.capstone.capstone.repository;

import com.capstone.capstone.entity.SlotHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlotHistoryRepository extends JpaRepository<SlotHistory, UUID> {
}
