package com.capstone.capstone.dto.response.report;

import com.capstone.capstone.dto.enums.ReportStatusEnum;
import com.capstone.capstone.dto.enums.ReportTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetAllReportResponse {
    private UUID reportId;
    private UUID employeeId;
    private UUID  residentId;
    private UUID roomId;
    private UUID semesterId;
    private String employeeName;
    private String content;
    private String responseMessage;
    private LocalDateTime createdDate;
    private ReportStatusEnum reportStatus;
    private ReportTypeEnum reportType;
    private String userCode;
}
