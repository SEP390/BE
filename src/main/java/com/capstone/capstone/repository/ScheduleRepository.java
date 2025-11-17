package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.Schedule;
import com.capstone.capstone.entity.Shift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    boolean existsByEmployeeAndWorkDateAndShift(Employee employee, LocalDate workDate, Shift shift);

    Page<Schedule> findByEmployee(Employee employee, Pageable pageable);

    Page<Schedule> findAll(Pageable pageable);

    // Tất cả schedule trong khoảng ngày
    List<Schedule> findAllByWorkDateBetween(LocalDate from, LocalDate to);

    // Schedule của 1 employee trong khoảng ngày
    List<Schedule> findAllByEmployee_IdAndWorkDateBetween(UUID employeeId,
                                                       LocalDate from,
                                                       LocalDate to);
}
