package com.capstone.capstone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Thông tin hóa đơn đặt phòng
 */
@Entity
@Getter
@Setter
public class SlotInvoice extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    // clone slot information, nullable (for slot delete in room update)
    private UUID slotId;
    private String slotName;

    private Long price;
}
