package com.capstone.capstone.repository;

import com.capstone.capstone.dto.enums.ReportTypeEnum;
import com.capstone.capstone.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByReportType(ReportTypeEnum reportType);

    List<Report> findByEmployeeId(UUID employeeId);
}
