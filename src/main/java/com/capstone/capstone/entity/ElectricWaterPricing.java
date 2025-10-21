package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ElectricWaterPricing extends BaseEntity {
    private Long electricPrice;
    private Long waterPrice;
    private LocalDateTime startDate;
}
