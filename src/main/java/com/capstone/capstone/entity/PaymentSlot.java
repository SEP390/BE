package com.capstone.capstone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Thông thanh toán cho đặt phòng
 */
@Entity
@Getter
@Setter
public class PaymentSlot extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "semester_id")
    private Semester semester;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne(optional = false)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    // clone slot information, nullable (for slot delete in room update)
    private UUID slotId;
    private String slotName;
    private String roomNumber;
    private String dormName;
    private Long price;
}
