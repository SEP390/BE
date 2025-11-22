package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ew_usage")
public class EWUsage extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    private Integer electric;
    private Integer water;
    private Boolean paid;

    private LocalDate startDate;
    private LocalDate endDate;
}
