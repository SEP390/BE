package com.capstone.capstone.repository;

import com.capstone.capstone.entity.TimeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeConfigRepository extends JpaRepository<TimeConfig, UUID>, JpaSpecificationExecutor<TimeConfig> {
    @Query("""
            FROM TimeConfig tc
            ORDER BY tc.createTime DESC
            LIMIT 1
            """)
    Optional<TimeConfig> findCurrent();
}
