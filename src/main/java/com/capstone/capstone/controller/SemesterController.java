package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.semester.CreateSemesterRequest;
import com.capstone.capstone.dto.request.semester.UpdateSemesterRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import com.capstone.capstone.service.impl.SemesterService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SemesterController {
    private final SemesterService semesterService;

    @GetMapping("/api/semesters")
    public BaseResponse<List<SemesterResponse>> getAll(
            @RequestParam(required = false) String name,
            @PageableDefault Pageable pageable) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", semesterService.getAll(name, pageable));
    }

    @GetMapping("/api/semesters/current")
    public BaseResponse<SemesterResponse> getCurrent() {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", semesterService.getCurrentResponse());
    }

    @GetMapping("/api/semesters/{id}")
    public BaseResponse<SemesterResponse> getByName(@PathVariable UUID id) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", semesterService.getResponseById(id));
    }

    @PostMapping("/api/semesters")
    public BaseResponse<SemesterResponse> create(@RequestBody CreateSemesterRequest request) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", semesterService.create(request));
    }

    @PutMapping("/api/semesters")
    public BaseResponse<SemesterResponse> update(@RequestBody UpdateSemesterRequest request) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", semesterService.update(request));
    }
}
