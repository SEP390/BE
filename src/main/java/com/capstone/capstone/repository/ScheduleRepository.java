package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.Schedule;
import com.capstone.capstone.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    boolean existsByEmployeeAndWorkDateAndShift(Employee employee, LocalDate workDate, Shift shift);
}
