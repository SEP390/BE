package com.capstone.capstone.dto.request.report;

import com.capstone.capstone.dto.enums.ReportStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateReportRequest {
    private UUID reportId;
    private ReportStatusEnum reportStatus;
    private String responseMessage;
}
