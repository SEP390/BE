package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SlotRepository extends JpaRepository<Slot, UUID>, JpaSpecificationExecutor<Slot> {
    Slot findByUser(User user);

    void deleteAllByRoom(Room room);

    List<Slot> findByRoom(Room room);
}
