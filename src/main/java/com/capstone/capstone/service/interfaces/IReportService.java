package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.report.CreateReportRequest;
import com.capstone.capstone.dto.request.report.UpdateReportRequest;
import com.capstone.capstone.dto.response.report.CreateReportResponse;
import com.capstone.capstone.dto.response.report.GetAllReportResponse;
import com.capstone.capstone.dto.response.report.UpdateReportResponse;

import java.util.List;
import java.util.UUID;

public interface IReportService {
    CreateReportResponse createReport(CreateReportRequest request);
    List<GetAllReportResponse> getAllReports();
    UpdateReportResponse updateReport(UUID requestId, UpdateReportRequest request);
}
