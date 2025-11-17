package com.capstone.capstone.repository;

import com.capstone.capstone.entity.EWRoom;
import com.capstone.capstone.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EWRoomRepository extends JpaRepository<EWRoom, UUID>, JpaSpecificationExecutor<EWRoom> {
    @Query("""
            FROM EWRoom r
            WHERE r.room = :room
            ORDER BY r.createTime DESC
            LIMIT 1
            """)
    Optional<EWRoom> findLatest(Room room);
}
