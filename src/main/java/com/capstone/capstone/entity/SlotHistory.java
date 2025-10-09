package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.StatusSlotHistoryEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlotHistory extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id")
    @JsonIgnore
    private Slot slot;

    @OneToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    private LocalDateTime createDate;
}
