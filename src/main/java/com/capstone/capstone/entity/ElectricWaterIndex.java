package com.capstone.capstone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chỉ số điện nước của phòng
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ElectricWaterIndex extends BaseEntity {
    /**
     * Chỉ số điện hiện tại, đơn vị kilowatt
     */
    private Integer electricIndex;

    /**
     * Chỉ số nước hiện tại, đơn vị khối (m3)
     */
    private Integer waterIndex;

    /**
     * Thời gian Bảo vệ ghi chỉ số
     */
    private LocalDateTime createDate;

    /**
     * Kỳ bảo vệ ghi chỉ số
     */
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;

    /**
     * Phòng cần ghi chỉ số
     */
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    /**
     * Hóa đơn điện nước của phòng
     */
    @OneToMany(mappedBy = "index")
    List<ElectricWaterBill> bills;
}
