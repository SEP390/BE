package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Shift extends BaseEntity {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;

    @OneToMany(mappedBy = "shift")
    List<Schedule> schedules;

}
