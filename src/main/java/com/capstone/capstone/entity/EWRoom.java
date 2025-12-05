package com.capstone.capstone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Chỉ số điện nước của phòng
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ew_room")
public class EWRoom extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @OneToMany(mappedBy = "ewRoom")
    private List<EWUsage> usages;

    private Integer electric;
    private Integer water;
    private Integer electricUsed;
    private Integer waterUsed;
    private LocalDate createDate;
}
