package com.capstone.capstone.repository;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {
    Optional<Invoice> findByUserAndTypeAndStatus(User user, InvoiceType type, PaymentStatus status);

    @Query("""
            FROM Invoice i
            WHERE i.user = :user AND i.type = com.capstone.capstone.dto.enums.InvoiceType.BOOKING
            ORDER BY i.createTime DESC
            LIMIT 1
            """)
    Optional<Invoice> findLatestBookingInvoice(User user);
}
