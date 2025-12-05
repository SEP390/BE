package com.capstone.capstone.repository;

import com.capstone.capstone.entity.EWInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EWInvoiceRepository extends JpaSpecificationExecutor<EWInvoice>, JpaRepository<EWInvoice, UUID> {
}
