package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PaymentElectricWater extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "bill_id")
    private ElectricWaterBill bill;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;
}
