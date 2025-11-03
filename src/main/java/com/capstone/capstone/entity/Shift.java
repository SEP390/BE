package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
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
public class Shift extends BaseEntity {
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
