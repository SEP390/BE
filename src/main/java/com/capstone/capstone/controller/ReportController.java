package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.report.CreateReportRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.report.CreateReportResponse;
import com.capstone.capstone.dto.response.request.CreateRequestResponse;
import com.capstone.capstone.entity.BaseEntity;
import com.capstone.capstone.service.interfaces.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
