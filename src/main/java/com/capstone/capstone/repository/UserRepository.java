package com.capstone.capstone.repository;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    List<User> findUserByRole(RoleEnum role);

}
