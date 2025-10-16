package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.semester.UpdateSemesterRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.request.semester.CreateSemesterRequest;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import com.capstone.capstone.service.impl.SemesterService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class SemesterController {
    private final SemesterService semesterService;

    @GetMapping("/api/semesters")
    public BaseResponse<?> getAll() {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", semesterService.getAll());
    }

    @GetMapping("/api/semesters/current")
    public BaseResponse<?> getCurrent() {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", semesterService.getCurrent());
    }

    @GetMapping("/api/semesters/{id}")
    public BaseResponse<SemesterResponse> getByName(@PathVariable UUID id) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", semesterService.getById(id));
    }

    @PostMapping("/api/semesters")
    public BaseResponse<?> create(@RequestBody CreateSemesterRequest request) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", semesterService.create(request));
    }

    @PutMapping("/api/semesters")
    public BaseResponse<?> update(@RequestBody UpdateSemesterRequest request) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", semesterService.update(request));
    }
}
