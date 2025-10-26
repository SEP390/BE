package com.capstone.capstone.dto.response.report;

import com.capstone.capstone.dto.enums.ReportStatusEnum;
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
public class UpdateReportResponse {
    private UUID reportId;
    private UUID employeeId;
    private String content;
    private String responseMessage;
    private LocalDateTime createdDate;
    private ReportStatusEnum reportStatus;
    private String userCode;
}
