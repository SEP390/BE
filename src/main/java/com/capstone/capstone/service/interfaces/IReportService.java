package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.report.CreateReportRequest;
import com.capstone.capstone.dto.response.report.CreateReportResponse;

public interface IReportService {
    CreateReportResponse createReport(CreateReportRequest request);
}
