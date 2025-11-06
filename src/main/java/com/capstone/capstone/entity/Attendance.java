package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.AttendanceStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_emp_date_shift",
                        columnNames = {"employee_id", "attendance_date", "shift_id"}
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Attendance extends BaseEntity {
    // --- Tham chiếu tới kế hoạch (có thể null nếu chấm công không theo lịch)
    @OneToOne
    @JoinColumn(name = "schedule_id", unique = true)
    private Schedule schedule;

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

    // --- Ngày làm việc thực tế
    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    // --- Thời điểm vào/ra (nếu chỉ có checkin thì checkout có thể null)
    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime;

    // --- Trạng thái chấm công
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AttendanceStatusEnum status;

    // --- Ghi chú (đi muộn, về sớm, lý do…)
    private String note;
}
