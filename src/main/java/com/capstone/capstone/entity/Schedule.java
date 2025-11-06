package com.capstone.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_schedule_emp_date_shift",
                        columnNames = {"employee_id", "work_date", "shift_id"}
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Schedule extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "shift_id")
    @JsonIgnore
    private Shift shift;

    @ManyToOne
    @JoinColumn(name = "semester_id")
    @JsonIgnore
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "dorm_id")
    @JsonIgnore
    private Dorm dorm;

    private LocalDate workDate;
    private String note;

    @OneToOne(mappedBy = "schedule", cascade = CascadeType.ALL)
    @JsonIgnore
    private Attendance attendance;
}
