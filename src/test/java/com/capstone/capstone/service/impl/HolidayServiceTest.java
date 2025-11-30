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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HolidayServiceTest {

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @InjectMocks
    private HolidayService holidayService;

    private Semester semester;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        semester = new Semester();
        semester.setId(UUID.randomUUID());
    }

    // ----------------------------------------------------------------------
    // ⭐ TEST CREATE HOLIDAY
    // ----------------------------------------------------------------------

    /**
     * TC1 – Validate: semesterId null → throw BadHttpRequestException
     */
    @Test
    void createHoliday_shouldFail_whenSemesterIdNull() {
        CreateHolidayRequest req = new CreateHolidayRequest();
        req.setSemesterId(null);

        BadHttpRequestException ex = assertThrows(
                BadHttpRequestException.class,
                () -> holidayService.createHoliday(req)
        );

        assertTrue(ex.getMessage().contains("Semester id is required"));
        verifyNoInteractions(semesterRepository, holidayRepository);
    }

    /**
     * TC2 – Validate: holidayName null/blank → throw BadHttpRequestException
     */
    @Test
    void createHoliday_shouldFail_whenNameBlank() {
        CreateHolidayRequest req = new CreateHolidayRequest();
        req.setSemesterId(UUID.randomUUID());
        req.setHolidayName("   ");

        BadHttpRequestException ex = assertThrows(
                BadHttpRequestException.class,
                () -> holidayService.createHoliday(req)
        );

        assertTrue(ex.getMessage().contains("Holiday name is required"));
        verifyNoInteractions(holidayRepository);
    }

    /**
     * TC3 – Validate: missing startDate or endDate
     */
    @Test
    void createHoliday_shouldFail_whenDatesMissing() {
        CreateHolidayRequest req = new CreateHolidayRequest();
        req.setSemesterId(UUID.randomUUID());
        req.setHolidayName("Tet Holiday");
        req.setStartDate(null);
        req.setEndDate(LocalDate.now());

        BadHttpRequestException ex = assertThrows(
                BadHttpRequestException.class,
                () -> holidayService.createHoliday(req)
        );

        assertTrue(ex.getMessage().contains("Start date and end date are required"));
    }

    /**
     * TC4 – Validate: endDate < startDate → throw BadHttpRequestException
     */
    @Test
    void createHoliday_shouldFail_whenEndBeforeStart() {
        CreateHolidayRequest req = new CreateHolidayRequest();
        req.setSemesterId(UUID.randomUUID());
        req.setHolidayName("Holiday");
        req.setStartDate(LocalDate.of(2024, 5, 10));
        req.setEndDate(LocalDate.of(2024, 5, 1));

        BadHttpRequestException ex = assertThrows(
                BadHttpRequestException.class,
                () -> holidayService.createHoliday(req)
        );

        assertTrue(ex.getMessage().contains("End date must be"));
    }

    /**
     * TC5 – Validate: semester not found → throw NotFoundException
     */
    @Test
    void createHoliday_shouldFail_whenSemesterNotFound() {
        UUID semId = UUID.randomUUID();
        CreateHolidayRequest req = new CreateHolidayRequest();
        req.setSemesterId(semId);
        req.setHolidayName("Holiday");
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now());

        when(semesterRepository.findById(semId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> holidayService.createHoliday(req)
        );

        verifyNoInteractions(holidayRepository);
    }

    /**
     * TC6 – Happy Path: Create success
     */
    @Test
    void createHoliday_shouldCreateSuccessfully() {
        UUID semId = semester.getId();
        CreateHolidayRequest req = new CreateHolidayRequest();
        req.setSemesterId(semId);
        req.setHolidayName("Tet");
        req.setStartDate(LocalDate.of(2024, 2, 1));
        req.setEndDate(LocalDate.of(2024, 2, 5));

        Holiday saved = new Holiday();
        saved.setId(UUID.randomUUID());
        saved.setHolidayName(req.getHolidayName());
        saved.setStartDate(req.getStartDate());
        saved.setEndDate(req.getEndDate());
        saved.setSemester(semester);

        when(semesterRepository.findById(semId)).thenReturn(Optional.of(semester));
        when(holidayRepository.save(any(Holiday.class))).thenReturn(saved);

        CreateHolidayResponse res = holidayService.createHoliday(req);

        assertEquals("Tet", res.getHolidayName());
        assertEquals(req.getSemesterId(), res.getSemesterId());
        verify(holidayRepository, times(1)).save(any());
    }

    // ----------------------------------------------------------------------
    // ⭐ TEST UPDATE HOLIDAY
    // ----------------------------------------------------------------------

    /**
     * TC7 – Update: holidayId not found → throw NotFoundException
     */
    @Test
    void updateHoliday_shouldFail_whenHolidayNotFound() {
        UUID id = UUID.randomUUID();
        UpdateHolidayRequest req = new UpdateHolidayRequest();
        req.setSemesterId(semester.getId());
        req.setHolidayName("Update");
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now());

        when(holidayRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> holidayService.updateHoliday(id, req)
        );
    }

    /**
     * TC8 – Update: semester not found → throw NotFoundException
     */
    @Test
    void updateHoliday_shouldFail_whenSemesterNotFound() {
        UUID id = UUID.randomUUID();
        UpdateHolidayRequest req = new UpdateHolidayRequest();
        req.setSemesterId(UUID.randomUUID());
        req.setHolidayName("Name");
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now());

        Holiday h = new Holiday();
        h.setId(id);

        when(holidayRepository.findById(id)).thenReturn(Optional.of(h));
        when(semesterRepository.findById(req.getSemesterId())).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> holidayService.updateHoliday(id, req)
        );
    }

    /**
     * TC9 – Update: endDate < startDate → throw BadHttpRequestException
     */
    @Test
    void updateHoliday_shouldFail_whenEndBeforeStart() {
        UpdateHolidayRequest req = new UpdateHolidayRequest();
        req.setSemesterId(semester.getId());
        req.setHolidayName("Test");
        req.setStartDate(LocalDate.of(2024, 2, 10));
        req.setEndDate(LocalDate.of(2024, 2, 1));

        UUID id = UUID.randomUUID();

        assertThrows(
                BadHttpRequestException.class,
                () -> holidayService.updateHoliday(id, req)
        );
    }

    /**
     * TC10 – Happy path: update success
     */
    @Test
    void updateHoliday_shouldUpdateSuccessfully() {
        UUID id = UUID.randomUUID();

        Holiday holiday = new Holiday();
        holiday.setId(id);

        UpdateHolidayRequest req = new UpdateHolidayRequest();
        req.setSemesterId(semester.getId());
        req.setHolidayName("New Name");
        req.setStartDate(LocalDate.of(2024, 1, 1));
        req.setEndDate(LocalDate.of(2024, 1, 5));

        when(holidayRepository.findById(id)).thenReturn(Optional.of(holiday));
        when(semesterRepository.findById(req.getSemesterId())).thenReturn(Optional.of(semester));
        when(holidayRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateHolidayResponse res = holidayService.updateHoliday(id, req);

        assertEquals("New Name", res.getHolidayName());
        assertEquals(req.getStartDate(), res.getStartDate());
        assertEquals(req.getSemesterId(), res.getSemesterId());
    }

    // ----------------------------------------------------------------------
    // ⭐ TEST GET ALL HOLIDAY
    // ----------------------------------------------------------------------

    /**
     * TC11 – getAll: return empty list when no holidays
     */
    @Test
    void getAllHoliday_shouldReturnEmptyList() {
        when(holidayRepository.findAll()).thenReturn(Collections.emptyList());

        List<GetAllHolidayResponse> res = holidayService.getAllHoliday();

        assertNotNull(res);
        assertEquals(0, res.size());
    }

    /**
     * TC12 – getAll: success list
     */
    @Test
    void getAllHoliday_shouldReturnList() {
        Holiday h = new Holiday();
        h.setId(UUID.randomUUID());
        h.setHolidayName("Holiday");
        h.setStartDate(LocalDate.now());
        h.setEndDate(LocalDate.now());
        h.setSemester(semester);

        when(holidayRepository.findAll()).thenReturn(List.of(h));

        List<GetAllHolidayResponse> res = holidayService.getAllHoliday();

        assertEquals(1, res.size());
        assertEquals("Holiday", res.get(0).getHolidayName());
        assertEquals(semester.getId(), res.get(0).getSemesterId());
    }

    /**
     * TC13 – getAll: holiday without semester (dirty data)
     */
    @Test
    void getAllHoliday_shouldHandleNullSemester() {
        Holiday h = new Holiday();
        h.setId(UUID.randomUUID());
        h.setHolidayName("Holiday X");
        h.setSemester(null); // dirty data

        when(holidayRepository.findAll()).thenReturn(List.of(h));

        List<GetAllHolidayResponse> res = holidayService.getAllHoliday();

        assertNull(res.get(0).getSemesterId());
    }
}