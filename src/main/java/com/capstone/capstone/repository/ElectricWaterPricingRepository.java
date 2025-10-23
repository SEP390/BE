package com.capstone.capstone.repository;

import com.capstone.capstone.entity.ElectricWaterPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ElectricWaterPricingRepository extends JpaRepository<ElectricWaterPricing, UUID>, JpaSpecificationExecutor<ElectricWaterPricing> {
    @Query("""
    FROM ElectricWaterPricing
    ORDER BY startDate DESC
    LIMIT 1
    """)
    ElectricWaterPricing latestPricing();
}
