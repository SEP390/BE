package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private Long price;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}
