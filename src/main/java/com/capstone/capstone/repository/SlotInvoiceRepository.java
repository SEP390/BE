package com.capstone.capstone.repository;

import com.capstone.capstone.entity.SlotInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SlotInvoiceRepository extends JpaRepository<SlotInvoice, UUID>, JpaSpecificationExecutor<SlotInvoice> {

}
