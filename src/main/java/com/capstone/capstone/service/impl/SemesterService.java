package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.repository.SemesterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SemesterService {
    private final SemesterRepository semesterRepository;

    public Semester getNextSemester() {
        return semesterRepository.findNextSemester();
    }

    public Semester getCurrent() {
        return semesterRepository.findCurrent();
    }
}
