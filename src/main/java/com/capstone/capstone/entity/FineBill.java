package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineBill extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private Long price;
    private String reason;
}
