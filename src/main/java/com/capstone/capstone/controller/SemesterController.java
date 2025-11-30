package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.semester.CreateSemesterRequest;
import com.capstone.capstone.dto.request.semester.UpdateSemesterRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import com.capstone.capstone.service.impl.SemesterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SemesterController {
    private final SemesterService semesterService;

    @GetMapping("/api/semesters")
    public BaseResponse<PagedModel<SemesterResponse>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID id,
            @PageableDefault Pageable pageable) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("name", name);
        filter.put("id", id);
        return new BaseResponse<>(semesterService.getAll(filter, pageable));
    }

    @GetMapping("/api/semesters/current")
    public BaseResponse<SemesterResponse> getCurrent() {
        return new BaseResponse<>(semesterService.getCurrentResponse());
    }

    @GetMapping("/api/semesters/next")
    public BaseResponse<SemesterResponse> getNext() {
        return new BaseResponse<>(semesterService.getNextResponse());
    }

    @GetMapping("/api/semesters/{id}")
    public BaseResponse<SemesterResponse> getByName(@PathVariable UUID id) {
        return new BaseResponse<>(semesterService.getResponseById(id));
    }

    @PostMapping("/api/semesters")
    public BaseResponse<SemesterResponse> create(@RequestBody CreateSemesterRequest request) {
        return new BaseResponse<>(semesterService.create(request));
    }

    @PostMapping("/api/semesters/{id}")
    public BaseResponse<SemesterResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateSemesterRequest request) {
        return new BaseResponse<>(semesterService.update(id, request));
    }

    @DeleteMapping("/api/semesters/{id}")
    public BaseResponse<SemesterResponse> delete(@PathVariable UUID id) {
        return new BaseResponse<>(semesterService.delete(id));
    }
}
