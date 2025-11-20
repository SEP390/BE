package com.capstone.capstone.dto.enums;

public enum RequestTypeEnum {
    CHECKOUT,                       // Yêu cầu checkout
    METER_READING_DISCREPANCY,      // Kiểm tra sai số điện/nước
    SECURITY_INCIDENT,              // Sự cố an ninh (đánh nhau, mất trộm, nghi vấn…)
    TECHNICAL_ISSUE,                // Sự cố kỹ thuật (hỏng điện/nước/thiết bị)
    POLICY_VIOLATION_REPORT,
    CHANGEROOM,
    ANONYMOUSE,// Báo cáo vi phạm quy định (report người khác)
    OTHER                           // Khác
}
