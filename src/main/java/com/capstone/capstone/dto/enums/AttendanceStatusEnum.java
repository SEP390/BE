package com.capstone.capstone.dto.enums;

public enum AttendanceStatusEnum {
    ON_TIME,                // có mặt, đủ ca, đúng giờ
    LATE,                   // đi muộn, nhưng tan ca đúng giờ
    EARLY_LEAVE,            // đến đúng giờ nhưng về sớm
    LATE_AND_EARLY_LEAVE,   // vừa đi muộn vừa về sớm

    ABSENT,                 // vắng không lý do
    ON_LEAVE                // nghỉ phép / đi công tác (được duyệt)
}
