package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.report.CreateReportRequest;
import com.capstone.capstone.dto.request.report.UpdateReportRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.report.CreateReportResponse;
import com.capstone.capstone.dto.response.report.GetAllReportResponse;
import com.capstone.capstone.dto.response.report.UpdateReportResponse;
import com.capstone.capstone.dto.response.request.CreateRequestResponse;
import com.capstone.capstone.dto.response.request.GetAllRequestResponse;
import com.capstone.capstone.entity.BaseEntity;
import com.capstone.capstone.service.interfaces.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.REPORTS.REPORT)
public class ReportController {

    private final IReportService iReportService;

    @PostMapping()
    public ResponseEntity<BaseResponse<CreateReportResponse>> createReport(@RequestBody CreateReportRequest createReportRequest) {
        CreateReportResponse createReportResponse = iReportService.createReport(createReportRequest);
        BaseResponse<CreateReportResponse> response = new BaseResponse<>(createReportResponse);
        response.setData(createReportResponse);
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("Report created successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<GetAllReportResponse>>> GetAllReports() {
        List<GetAllReportResponse> reports = iReportService.getAllReports();
        BaseResponse<List<GetAllReportResponse>> response = new BaseResponse<>();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Report read successfully");
        response.setData(reports);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(ApiConstant.REPORTS.GET_BY_ID)
    public ResponseEntity<BaseResponse<UpdateReportResponse>> updateReportById(@PathVariable UUID reportId, @RequestBody UpdateReportRequest requestUpdate) {
        UpdateReportResponse response = iReportService.updateReport(reportId, requestUpdate);
        BaseResponse<UpdateReportResponse> methodResponse = new BaseResponse<>();
        methodResponse.setStatus(HttpStatus.OK.value());
        methodResponse.setMessage("Report updated successfully");
        methodResponse.setData(response);
        return ResponseEntity.status(HttpStatus.OK).body(methodResponse);
    }
}
