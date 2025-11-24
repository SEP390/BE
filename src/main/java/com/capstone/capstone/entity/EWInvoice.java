package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import java.time.LocalDate;

@Entity
public class EWInvoice extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    private Integer electricUsed;
    private Integer waterUsed;
    private LocalDate startDate;
    private LocalDate endDate;
}
