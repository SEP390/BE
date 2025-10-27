package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.RoomPricing;
import com.capstone.capstone.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomPricingRepository extends JpaRepository<RoomPricing, UUID>, JpaSpecificationExecutor<RoomPricing> {

    Optional<RoomPricing> findByTotalSlot(int totalSlot);

    @Query("""
        FROM RoomPricing rp
        JOIN Room r ON r.totalSlot = rp.totalSlot
        AND r = :room
    """)
    Optional<RoomPricing> findByRoom(Room room);

    @Query("""
        FROM RoomPricing rp
        JOIN Slot s ON s = :slot AND s.room.totalSlot = rp.totalSlot
    """)
    Optional<RoomPricing> findBySlot(Slot slot);
}
