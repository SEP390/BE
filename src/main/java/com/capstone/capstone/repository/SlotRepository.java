package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlotRepository extends JpaRepository<Slot, UUID> {
    Slot findByUser(User user);
}
