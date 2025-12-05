package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class EWInvoice extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    private Integer electricUsed;
    private Integer waterUsed;
    private LocalDate startDate;
    private LocalDate endDate;
}
