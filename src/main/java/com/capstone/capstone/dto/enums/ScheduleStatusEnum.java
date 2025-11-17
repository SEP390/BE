package com.capstone.capstone.dto.enums;

public enum ScheduleStatusEnum {
    NOT_CHECKED_IN,   // Chưa chấm công
    CHECKED_IN,       // Đã chấm công (có bản ghi attendance)
    ABSENT,           // Đã “chốt” là vắng (ca này lẽ ra phải làm nhưng không chấm, và manager confirm vắng)
}
