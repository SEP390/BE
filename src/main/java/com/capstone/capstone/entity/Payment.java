package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.PaymentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment extends BaseEntity {
    private LocalDateTime createDate;
    private long price;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentType type;

    private String note;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_history_id")
    private SlotHistory slotHistory;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "electric_water_bill_id")
    private ElectricWaterBill electricWaterBill;
}
