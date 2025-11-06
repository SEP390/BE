package com.capstone.capstone.repository;

import com.capstone.capstone.entity.ElectricWaterIndex;
import com.capstone.capstone.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ElectricWaterIndexRepository extends JpaRepository<ElectricWaterIndex, UUID> {

    ElectricWaterIndex findByRoom(Room room);
}
