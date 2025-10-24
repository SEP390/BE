package com.capstone.capstone.dto.response.report;

import com.capstone.capstone.dto.enums.ReportStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateReportResponse {
    private String content;
    private LocalDateTime createdAt;
    private ReportStatusEnum reportStatus;
    private String responseMessage;
    private String userCode;
}
