package com.capstone.capstone.repository;

import com.capstone.capstone.entity.RoomPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomPricingRepository extends JpaRepository<RoomPricing, UUID> {

    RoomPricing findByTotalSlot(int totalSlot);
}
