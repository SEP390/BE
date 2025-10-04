package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RoomPricing extends BaseEntity {
    private long price;
    private int totalSlot;
}