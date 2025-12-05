package com.capstone.capstone.dto.request.report;

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
public class CreateReportRequest {
    private String content;
    private UUID residentId;
    private UUID roomId;
    private LocalDateTime createAt;
    private ReportTypeEnum reportType;
}
