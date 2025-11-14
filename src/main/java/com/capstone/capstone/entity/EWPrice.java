package com.capstone.capstone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EWPrice extends BaseEntity {
    /**
     * Giá điện nếu vượt quá maxElectric/user (đ/kw)
     */
    @Column(nullable = false)
    private Long electric;
    /**
     * Giá nước nếu vượt quá maxWater/user (đ/m3)
     */
    @Column(nullable = false)
    private Long water;
    @Column(nullable = false)
    private Long maxElectric;
    @Column(nullable = false)
    private Long maxWater;
    /**
     * Thời gian bắt đầu có hiệu lực
     */
    @Column(nullable = false)
    private LocalDateTime effectiveTime;
}
