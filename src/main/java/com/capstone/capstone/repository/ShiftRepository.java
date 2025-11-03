package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


public interface ShiftRepository extends JpaRepository<Shift, UUID> {
}
