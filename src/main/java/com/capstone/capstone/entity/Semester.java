package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Semester extends BaseEntity {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
