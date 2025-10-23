package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.semester.CreateSemesterRequest;
import com.capstone.capstone.dto.request.semester.UpdateSemesterRequest;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SemesterRepository;
import com.capstone.capstone.util.SortUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SemesterService {
    private final SemesterRepository semesterRepository;
    private final ModelMapper modelMapper;

    public Semester getNext() {
        return semesterRepository.findNextSemester();
    }

    public Semester getCurrent() {
        return semesterRepository.findCurrent();
    }

    public SemesterResponse getCurrentResponse() {
        return modelMapper.map(semesterRepository.findCurrent(), SemesterResponse.class);
    }

    public List<SemesterResponse> getAll(String name, Pageable pageable) {
        Sort sort = SortUtil.getSort(pageable, "startDate");
        return semesterRepository.findAll(
                (name != null && !name.isBlank()) ? (r,q,c) -> c.equal(r.get("name"), name) : Specification.unrestricted(),
                sort
        ).stream().map(semester -> modelMapper.map(semester, SemesterResponse.class)).toList();
    }

    public SemesterResponse getResponseById(UUID id) {
        return modelMapper.map(semesterRepository.findById(id).orElseThrow(() -> new AppException("SEMESTER_NOT_FOUND")), SemesterResponse.class);
    }

    public Semester getById(UUID id) {
        return semesterRepository.findById(id).orElse(null);
    }

    public SemesterResponse create(CreateSemesterRequest request) {
        if (semesterRepository.exists((root, query, cb) -> cb.equal(root.get("name"), request.getName()))) {
            throw new AppException("SEMESTER_NAME_EXISTED");
        }
        // TODO: check overlapping date
        return modelMapper.map(semesterRepository.save(modelMapper.map(request,Semester.class)), SemesterResponse.class);
    }

    public SemesterResponse update(UpdateSemesterRequest request) {
        if (!semesterRepository.existsById(request.getId())) {
            throw new AppException("SEMESTER_NOT_FOUND");
        }
        // TODO: check overlapping date
        return modelMapper.map(semesterRepository.save(modelMapper.map(request,Semester.class)), SemesterResponse.class);
    }
}
