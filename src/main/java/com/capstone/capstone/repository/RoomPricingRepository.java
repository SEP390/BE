package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.RoomPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomPricingRepository extends JpaRepository<RoomPricing, UUID> {

    RoomPricing findByTotalSlot(int totalSlot);

    @Query("""
        FROM RoomPricing rp
        JOIN Room r ON r.totalSlot = rp.totalSlot
        AND r = :room
    """)
    RoomPricing findByRoom(Room room);
}
