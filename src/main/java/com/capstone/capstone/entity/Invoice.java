package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Invoice extends BaseEntity {
    private LocalDateTime createDate;
    private long price;
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    private String note;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
