package com.capstone.capstone.repository;

import com.capstone.capstone.entity.ElectricWaterBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ElectricWaterBillRepository extends JpaRepository<ElectricWaterBill, UUID> {

}
