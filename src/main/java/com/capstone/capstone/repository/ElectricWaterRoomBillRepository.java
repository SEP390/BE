package com.capstone.capstone.repository;

import com.capstone.capstone.entity.ElectricWaterRoomBill;
import com.capstone.capstone.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ElectricWaterRoomBillRepository extends JpaRepository<ElectricWaterRoomBill, UUID> {

    ElectricWaterRoomBill findByRoom(Room room);
}
