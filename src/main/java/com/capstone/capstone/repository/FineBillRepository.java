package com.capstone.capstone.repository;

import com.capstone.capstone.entity.FineBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FineBillRepository extends JpaRepository<FineBill, Long>, JpaSpecificationExecutor<FineBill> {

}
