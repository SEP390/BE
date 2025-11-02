package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
}
