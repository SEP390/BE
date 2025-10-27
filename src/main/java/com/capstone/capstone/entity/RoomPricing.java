package com.capstone.capstone.entity;

import jakarta.persistence.Column;
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
    @Column(nullable = false)
    private Long price;
    @Column(unique = true, nullable = false)
    private Integer totalSlot;

    @OneToMany(mappedBy = "pricing")
    private List<Room> rooms;
}