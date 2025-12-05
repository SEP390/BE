package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TimeConfig extends BaseEntity {
    private LocalDate startBookingDate;
    private LocalDate endBookingDate;
    private LocalDate startExtendDate;
    private LocalDate endExtendDate;
    private LocalDateTime createTime;
}
