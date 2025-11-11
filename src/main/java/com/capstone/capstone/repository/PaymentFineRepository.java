package com.capstone.capstone.repository;

import com.capstone.capstone.entity.PaymentFine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentFineRepository extends JpaRepository<PaymentFine, Long>, JpaSpecificationExecutor<PaymentFine> {
}
