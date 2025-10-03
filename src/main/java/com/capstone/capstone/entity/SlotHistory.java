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
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "slot_id")
    @JsonIgnore
    private Slot slot;

    private LocalDateTime createDate;

    @Enumerated(EnumType.STRING)
    private StatusSlotHistoryEnum status;
}
