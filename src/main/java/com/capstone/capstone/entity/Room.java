package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Room extends BaseEntity {
    private String roomNumber;
    private Integer totalSlot;
    private Integer floor;

    @Enumerated(EnumType.STRING)
    private StatusRoomEnum status;
    @OneToMany(mappedBy = "room")
    private List<Slot> slots;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dorm_id")
    @JsonIgnore
    private Dorm dorm;

    @ManyToOne
    @JoinColumn(name = "pricing_id")
    private RoomPricing pricing;

    @OneToMany(mappedBy = "room")
    private List<Report> reports;
}
