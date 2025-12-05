package com.capstone.capstone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ew_price")
public class EWPrice extends BaseEntity {
    /**
     * Giá điện nếu vượt quá maxElectricIndex/user (đ/kw)
     */
    @Column
    private Long electricPrice;
    /**
     * Giá nước nếu vượt quá maxWaterIndex/user (đ/m3)
     */
    @Column
    private Long waterPrice;
    @Column
    private Integer maxElectricIndex;
    @Column
    private Integer maxWaterIndex;
    /**
     * Thời gian bắt đầu có hiệu lực
     */
    @Column
    private LocalDateTime createTime;
}
