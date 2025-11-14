package com.capstone.capstone.dto.request.report;

import com.capstone.capstone.dto.enums.ReportTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateReportRequest {
    private String content;
    private LocalDateTime createAt;
    private ReportTypeEnum reportType;
}
