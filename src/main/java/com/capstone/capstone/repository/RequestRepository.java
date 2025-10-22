package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Request;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, UUID> {
    @Query("""
    select r from Request r where r.user = :user
""")
    List<Request> findRequestByUser(@Param("user") User user);
}
