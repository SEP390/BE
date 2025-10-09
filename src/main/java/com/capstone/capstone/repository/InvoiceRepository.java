package com.capstone.capstone.repository;

import com.capstone.capstone.dto.response.booking.InvoiceResponse;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Page<Invoice> findByUser(User user, Pageable pageable);
}
