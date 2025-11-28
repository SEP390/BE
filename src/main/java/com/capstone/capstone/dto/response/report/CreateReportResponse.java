package com.capstone.capstone.dto.response.report;

import com.capstone.capstone.dto.enums.ReportStatusEnum;
import com.capstone.capstone.dto.enums.ReportTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateReportResponse {
    private UUID reportId;
    private String content;
    private LocalDateTime createdAt;
    private ReportStatusEnum reportStatus;
    private String responseMessage;
    private String userCode;
    private ReportTypeEnum reportType;
}
