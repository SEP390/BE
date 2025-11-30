package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.holiday.CreateHolidayRequest;
import com.capstone.capstone.dto.request.holiday.UpdateHolidayRequest;
import com.capstone.capstone.dto.response.holiday.CreateHolidayResponse;
import com.capstone.capstone.dto.response.holiday.GetAllHolidayResponse;
import com.capstone.capstone.dto.response.holiday.UpdateHolidayResponse;
import com.capstone.capstone.entity.Holiday;
import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.HolidayRepository;
import com.capstone.capstone.repository.SemesterRepository;
import com.capstone.capstone.service.interfaces.IHolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HolidayService implements IHolidayService {
    private final HolidayRepository holidayRepository;
    private final SemesterRepository semesterRepository;

    @Override
    public CreateHolidayResponse createHoliday(CreateHolidayRequest request) {
        // ✅ Validate input cơ bản
        if (request.getSemesterId() == null) {
            throw new BadHttpRequestException("Semester id is required");
        }
        if (request.getHolidayName() == null || request.getHolidayName().trim().isEmpty()) {
            throw new BadHttpRequestException("Holiday name is required");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BadHttpRequestException("Start date and end date are required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadHttpRequestException("End date must be on or after start date");
        }

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new NotFoundException("Semester not found"));

        Holiday holiday = new Holiday();
        holiday.setHolidayName(request.getHolidayName());
        holiday.setStartDate(request.getStartDate());
        holiday.setEndDate(request.getEndDate());
        holiday.setSemester(semester);
        holiday = holidayRepository.save(holiday);

        CreateHolidayResponse response = new CreateHolidayResponse();
        response.setHolidayName(holiday.getHolidayName());
        response.setStartDate(holiday.getStartDate());
        response.setEndDate(holiday.getEndDate());
        response.setSemesterId(request.getSemesterId());
        return response;
    }

    @Override
    public UpdateHolidayResponse updateHoliday(UUID holidayId, UpdateHolidayRequest req) {
        // ✅ Validate input cơ bản
        if (req.getSemesterId() == null) {
            throw new BadHttpRequestException("Semester id is required");
        }
        if (req.getHolidayName() == null || req.getHolidayName().trim().isEmpty()) {
            throw new BadHttpRequestException("Holiday name is required");
        }
        if (req.getStartDate() == null || req.getEndDate() == null) {
            throw new BadHttpRequestException("Start date and end date are required");
        }
        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new BadHttpRequestException("End date must be on or after start date");
        }

        Holiday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new NotFoundException("Holiday not found"));

        Semester semester = semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new NotFoundException("Semester not found"));

        holiday.setHolidayName(req.getHolidayName());
        holiday.setStartDate(req.getStartDate());
        holiday.setEndDate(req.getEndDate());
        holiday.setSemester(semester);
        holidayRepository.save(holiday);
        UpdateHolidayResponse response = new UpdateHolidayResponse();
        response.setHolidayName(holiday.getHolidayName());
        response.setStartDate(holiday.getStartDate());
        response.setEndDate(holiday.getEndDate());
        response.setSemesterId(holiday.getSemester().getId());
        return response;
    }

    @Override
    public List<GetAllHolidayResponse> getAllHoliday() {
        List<Holiday> holidays = holidayRepository.findAll();
        List<GetAllHolidayResponse> response = new ArrayList<>();
        for (Holiday holiday : holidays) {
            GetAllHolidayResponse responseHoliday = new GetAllHolidayResponse();
            responseHoliday.setHolidayId(holiday.getId());
            responseHoliday.setHolidayName(holiday.getHolidayName());
            responseHoliday.setStartDate(holiday.getStartDate());
            responseHoliday.setEndDate(holiday.getEndDate());
            // ⚠️ phòng NPE nếu dữ liệu xấu trong DB (semester = null)
            responseHoliday.setSemesterId(
                    holiday.getSemester() != null ? holiday.getSemester().getId() : null
            );
            response.add(responseHoliday);
        }
        return response;
    }
}
