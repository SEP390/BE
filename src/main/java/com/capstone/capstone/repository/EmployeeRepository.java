package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    @Query("""
    select e from Employee e where e.user = :user
""")
    Optional<Employee> findByUser(@Param("user") User user);

    Optional<Employee> findEmployeeByUser(User user);
}
