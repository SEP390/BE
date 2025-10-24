package com.capstone.capstone.repository;

import com.capstone.capstone.dto.response.room.RoomDetails;
import com.capstone.capstone.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
}
