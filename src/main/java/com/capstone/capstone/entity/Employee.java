package com.capstone.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Employee extends BaseEntity{
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "dorm_id", nullable = true)
    @JsonIgnore
    private Dorm dorm;

    @OneToMany(mappedBy = "employee")
    private List<Report> reports;

    @OneToMany(mappedBy = "employee")
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "employee")
    private List<Attendance> attendances;
}
