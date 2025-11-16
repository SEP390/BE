package com.capstone.capstone.repository;

import com.capstone.capstone.entity.EWPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EWPriceRepository extends JpaRepository<EWPrice, UUID>, JpaSpecificationExecutor<EWPrice> {
    @Query("""
            FROM EWPrice p
            ORDER BY p.createTime DESC
            LIMIT 1
            """)
    Optional<EWPrice> getCurrent();
}
