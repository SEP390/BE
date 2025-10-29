package com.capstone.capstone.repository;

import com.capstone.capstone.entity.PaymentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentSlotRepository extends JpaRepository<PaymentSlot, UUID>, JpaSpecificationExecutor<PaymentSlot> {
}
