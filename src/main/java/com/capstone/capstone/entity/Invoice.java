package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.InvoiceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Long price;
    private String reason;
    @Enumerated(EnumType.STRING)
    private InvoiceType type;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @OneToOne(mappedBy = "invoice")
    private SlotInvoice slotInvoice;

    @OneToOne(mappedBy = "invoice")
    private EWInvoice ewInvoice;

    @OneToMany(mappedBy = "invoice")
    private List<Payment> payments;
}
