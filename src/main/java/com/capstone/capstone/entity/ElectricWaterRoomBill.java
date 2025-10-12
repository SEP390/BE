package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ElectricWaterRoomBill extends BaseEntity {
    private long price;
    private int kw;
    private int m3;
    private LocalDateTime createDate;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}
