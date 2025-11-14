package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "semester_id")
    private Semester semester;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private UUID fromSlotId;
    private UUID fromRoomId;

    // clone slot information, nullable (for slot delete in room update)
    private UUID slotId;
    private UUID roomId;
    private String slotName;
    private String roomNumber;
    private String dormName;
    private Long price;

    private LocalDateTime checkin;
    private LocalDateTime checkout;
}
