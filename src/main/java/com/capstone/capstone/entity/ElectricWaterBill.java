package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ElectricWaterBill extends BaseEntity {
    private Long price;
    private LocalDateTime createDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_bill_id")
    private ElectricWaterRoomBill roomBill;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @OneToMany
    private List<Payment> payment;
}
