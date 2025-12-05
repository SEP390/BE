package com.capstone.capstone.dto.response.report;

import com.capstone.capstone.dto.enums.ReportStatusEnum;
import com.capstone.capstone.dto.enums.ReportTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetReportByIdResponse {
    private UUID reportId;
    private UUID employeeId;
    private String employeeName;
    private String content;
    private String responseMessage;
    private LocalDateTime createdDate;
    private ReportStatusEnum reportStatus;
    private ReportTypeEnum reportType;
    private String userCode;
    private UUID  residentId;
    private UUID roomId;
    private UUID semesterId;
}
