package com.capstone.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    @JsonIgnore
    private Shift shift;


    @ManyToOne
    @JoinColumn(name = "dorm_id", nullable = false)
    @JsonIgnore
    private Dorm dorm;

    @CreationTimestamp                 // Hibernate tự set khi INSERT
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp                   // Hibernate tự set khi UPDATE
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    private String note;
}
