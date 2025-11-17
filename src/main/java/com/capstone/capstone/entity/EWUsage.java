package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EWUsage extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    private Integer electric;
    private Integer water;
    private Boolean paid;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
