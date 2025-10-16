package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.semester.CreateSemesterRequest;
import com.capstone.capstone.dto.request.semester.UpdateSemesterRequest;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SemesterRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SemesterService {
    private final SemesterRepository semesterRepository;
    private final ModelMapper modelMapper;

    public Semester getNextSemester() {
        return semesterRepository.findNextSemester();
    }

    public Semester getCurrent() {
        return semesterRepository.findCurrent();
    }

    public List<SemesterResponse> getAll() {
        return semesterRepository.findAll().stream().map(semester -> modelMapper.map(semester, SemesterResponse.class)).toList();
    }

    public SemesterResponse getById(UUID id) {
        return modelMapper.map(semesterRepository.findById(id).orElseThrow(), SemesterResponse.class);
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
