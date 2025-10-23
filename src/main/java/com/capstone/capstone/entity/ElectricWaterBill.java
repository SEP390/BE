package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Hóa đơn điện nước của phòng
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ElectricWaterBill extends BaseEntity {
    private LocalDateTime createDate;
    /**
     * Tổng tiền điện nước của phòng
     */
    private Long totalPrice;
    /**
     * Tiền mỗi thành viên phải trả
     */
    private Long price;
    /**
     * Số user trong phòng hiện tại
     */
    private Integer userCount;

    /**
     * Chỉ số điện nước của phòng
     */
    @ManyToOne
    @JoinColumn(name = "room_bill_id")
    private ElectricWaterIndex index;
    /**
     * Trạng thái thanh toán, thành công nếu tất cả thành viên trong phòng đều dã trả tiền
     */
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
