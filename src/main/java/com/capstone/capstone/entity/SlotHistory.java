package com.capstone.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private UUID userId;
    @ManyToOne
    @JoinColumn(name = "slot_id")
    @JsonIgnore
    private Slot slot;

}
