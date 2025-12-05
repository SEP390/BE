package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {
    List<Payment> findAllByInvoice(Invoice invoice);
    @Query("""
    FROM Payment p
    WHERE p.invoice = :invoice
    ORDER BY p.createTime DESC
    LIMIT 1
    """)
    Optional<Payment> findLatestByInvoice(Invoice invoice);

    @Query("""
    FROM Payment p
    WHERE p.invoice = :invoice AND p.status = com.capstone.capstone.dto.enums.PaymentStatus.PENDING
    ORDER BY p.createTime DESC
    LIMIT 1
    """)
    Optional<Payment> findPendingByInvoice(Invoice invoice);
}
