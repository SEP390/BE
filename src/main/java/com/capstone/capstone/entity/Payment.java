package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private PaymentStatus status;
    private LocalDateTime createTime;
    private Long price;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}
