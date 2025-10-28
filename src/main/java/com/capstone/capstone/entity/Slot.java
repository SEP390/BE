package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Slot extends BaseEntity {
    private String slotName;
    @Enumerated(EnumType.STRING)
    private StatusSlotEnum status;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "room_id")
    @JsonIgnore
    private Room room;
}
