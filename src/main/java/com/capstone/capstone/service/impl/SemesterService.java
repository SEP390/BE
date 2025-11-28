package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.semester.CreateSemesterRequest;
import com.capstone.capstone.dto.request.semester.UpdateSemesterRequest;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SemesterRepository;
import com.capstone.capstone.util.SpecQuery;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SemesterService {
    private final SemesterRepository semesterRepository;
    private final ModelMapper modelMapper;

    public Semester getNext() {
        return semesterRepository.findNextSemester();
    }

    public Optional<Semester> getCurrent() {
        return Optional.ofNullable(semesterRepository.findCurrent());
    }

    public SemesterResponse getCurrentResponse() {
        return modelMapper.map(semesterRepository.findCurrent(), SemesterResponse.class);
    }

    public PagedModel<SemesterResponse> getAll(String name, Pageable pageable) {
        var query = new SpecQuery<Semester>();
        query.like("name", name);
        return new PagedModel<>(semesterRepository.findAll(query.and(), pageable).map(semester -> modelMapper.map(semester, SemesterResponse.class)));
    }

    public SemesterResponse getResponseById(UUID id) {
        return modelMapper.map(semesterRepository.findById(id).orElseThrow(() -> new AppException("SEMESTER_NOT_FOUND")), SemesterResponse.class);
    }

    public Semester getById(UUID id) {
        return semesterRepository.findById(id).orElse(null);
    }

    public SemesterResponse create(CreateSemesterRequest request) {
        Semester semester = modelMapper.map(request, Semester.class);
        semester = create(semester);
        return modelMapper.map(semester, SemesterResponse.class);
    }

    public Semester create(Semester semester) {
        if (semesterRepository.exists((root, query, cb) -> cb.equal(root.get("name"), semester.getName()))) {
            throw new AppException("SEMESTER_NAME_EXISTED");
        }
        if (semesterRepository.exists((r, q, c) -> {
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

    public SemesterResponse update(UUID id, UpdateSemesterRequest request) {
        if (!semesterRepository.existsById(id)) {
            throw new AppException("SEMESTER_NOT_FOUND");
        }
        Semester semester = modelMapper.map(request, Semester.class);
        semester.setId(id);
        if (semesterRepository.exists((r, q, c) -> {
            return c.and(c.notEqual(r.get("id"), id), c.or(
                    c.between(r.get("startDate"), semester.getStartDate(), semester.getEndDate()),
                    c.between(r.get("endDate"), semester.getStartDate(), semester.getEndDate())
            ));
        })) throw new AppException("SEMESTER_OVERLAPPING");
        return modelMapper.map(semesterRepository.save(semester), SemesterResponse.class);
    }

    public SemesterResponse delete(UUID id) {
        Semester semester = semesterRepository.findById(id).orElseThrow(() -> new AppException("SEMESTER_NOT_FOUND"));
        semesterRepository.delete(semester);
        return modelMapper.map(semester, SemesterResponse.class);
    }

    public SemesterResponse getNextResponse() {
        var next = getNext();
        if (next == null) throw new AppException("SEMESTER_NOT_FOUND");
        return modelMapper.map(next, SemesterResponse.class);
    }
}
