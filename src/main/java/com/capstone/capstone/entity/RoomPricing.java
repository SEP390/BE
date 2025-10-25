package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RoomPricing extends BaseEntity {
    private Long price;
    private Integer totalSlot;

    @OneToMany(mappedBy = "pricing")
    private List<Room> rooms;
}