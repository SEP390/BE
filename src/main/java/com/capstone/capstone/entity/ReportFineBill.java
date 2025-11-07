package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportFineBill extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;
    @ManyToOne
    @JoinColumn(name = "bill_id")
    private FineBill bill;
}
