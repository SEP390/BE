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

import java.time.LocalDate;
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
        Semester semester = semesterRepository.save(modelMapper.map(request,Semester.class));
        semester = create(semester);
        // TODO: check overlapping date
        return modelMapper.map(semester, SemesterResponse.class);
    }

    public Semester create(Semester semester) {
        if (semesterRepository.exists((root, query, cb) -> cb.equal(root.get("name"), semester.getName()))) {
            throw new AppException("SEMESTER_NAME_EXISTED");
        }
        if (semesterRepository.exists((r,q,c) -> {
            return c.or(
                    c.between(r.get("startDate"), semester.getStartDate(), semester.getEndDate()),
                    c.between(r.get("endDate"), semester.getStartDate(), semester.getEndDate())
            );
        })) throw new AppException("SEMESTER_OVERLAPPING");
        return semesterRepository.save(semester);
    }

    public Semester create(String semesterName, LocalDate startDate, LocalDate endDate) {
        Semester semester = new Semester();
        semester.setStartDate(startDate);
        semester.setEndDate(endDate);
        semester.setName(semesterName);
        return create(semester);
    }

    public SemesterResponse update(UpdateSemesterRequest request) {
        if (!semesterRepository.existsById(request.getId())) {
            throw new AppException("SEMESTER_NOT_FOUND");
        }
        // TODO: check overlapping date
        return modelMapper.map(semesterRepository.save(modelMapper.map(request,Semester.class)), SemesterResponse.class);
    }
}
