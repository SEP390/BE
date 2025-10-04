package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SlotRepository extends JpaRepository<Slot, UUID> {
    @Query("""
    FROM Slot s
    JOIN FETCH s.room
""")
    Slot getSlotWithRoom(UUID id);

    List<Slot> findByRoom(Room room);
}
