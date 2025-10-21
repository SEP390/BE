package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Guard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuardRepository extends JpaRepository<Guard, UUID> {
}
