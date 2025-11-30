package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.semester.CreateSemesterRequest;
import com.capstone.capstone.dto.request.semester.UpdateSemesterRequest;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SemesterServiceTest {

    // Common test data
    private final UUID semesterId = UUID.randomUUID();
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private SemesterService semesterService;
    private Semester mockSemester;
    private SemesterResponse mockSemesterResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        mockSemester = new Semester();
        mockSemester.setId(semesterId);
        mockSemester.setName("Fall 2025");
        mockSemester.setStartDate(LocalDate.of(2025, 9, 1));
        mockSemester.setEndDate(LocalDate.of(2025, 12, 31));

        mockSemesterResponse = new SemesterResponse();
        mockSemesterResponse.setId(semesterId);
        mockSemesterResponse.setName("Fall 2025");

        pageable = PageRequest.of(0, 10);
    }

    // --- getNext/getCurrent/Response Tests ---

    @Test
    @DisplayName("getNext: Should return the next semester if found")
    void getNext_Found() {
        when(semesterRepository.findNextSemester()).thenReturn(mockSemester);

        Semester result = semesterService.getNext();

        assertThat(result).isEqualTo(mockSemester);
    }

    @Test
    @DisplayName("getNext: Should return null if no next semester is found")
    void getNext_NotFound() {
        when(semesterRepository.findNextSemester()).thenReturn(null);

        Semester result = semesterService.getNext();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getNextResponse: Should return SemesterResponse for the next semester")
    void getNextResponse_Success() {
        when(semesterRepository.findNextSemester()).thenReturn(mockSemester);
        when(modelMapper.map(mockSemester, SemesterResponse.class)).thenReturn(mockSemesterResponse);

        SemesterResponse result = semesterService.getNextResponse();

        assertThat(result).isEqualTo(mockSemesterResponse);
    }

    @Test
    @DisplayName("getNextResponse: Should throw SEMESTER_NOT_FOUND if next semester is null")
    void getNextResponse_NotFound() {
        when(semesterRepository.findNextSemester()).thenReturn(null);

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> semesterService.getNextResponse())
                .withMessage("SEMESTER_NOT_FOUND");
    }

    @Test
    @DisplayName("getCurrent: Should return Optional containing the current semester")
    void getCurrent_Found() {
        when(semesterRepository.findCurrent()).thenReturn(mockSemester);

        Optional<Semester> result = semesterService.getCurrent();

        assertThat(result).contains(mockSemester);
    }

    @Test
    @DisplayName("getCurrent: Should return empty Optional if no current semester is found")
    void getCurrent_NotFound() {
        when(semesterRepository.findCurrent()).thenReturn(null);

        Optional<Semester> result = semesterService.getCurrent();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCurrentResponse: Should return SemesterResponse for the current semester")
    void getCurrentResponse_Success() {
        when(semesterRepository.findCurrent()).thenReturn(mockSemester);
        when(modelMapper.map(mockSemester, SemesterResponse.class)).thenReturn(mockSemesterResponse);

        SemesterResponse result = semesterService.getCurrentResponse();

        assertThat(result).isEqualTo(mockSemesterResponse);
    }

    // --- getAll Tests ---

    @Test
    @DisplayName("getAll: Should return PagedModel of SemesterResponse with name filtering")
    void getAll_Success() {
        // Arrange
        Page<Semester> semesterPage = new PageImpl<>(Collections.singletonList(mockSemester));
        when(semesterRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(semesterPage);
        when(modelMapper.map(any(Semester.class), eq(SemesterResponse.class))).thenReturn(mockSemesterResponse);

        // Act
        PagedModel<SemesterResponse> result = semesterService.getAll(null, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mockSemesterResponse);
        // Verify that a Specification containing the name filter was used
        verify(semesterRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // --- getById/ResponseById Tests ---

    @Test
    @DisplayName("getResponseById: Should return SemesterResponse when ID is found")
    void getResponseById_Found() {
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(mockSemester));
        when(modelMapper.map(mockSemester, SemesterResponse.class)).thenReturn(mockSemesterResponse);

        SemesterResponse result = semesterService.getResponseById(semesterId);

        assertThat(result).isEqualTo(mockSemesterResponse);
    }

    @Test
    @DisplayName("getResponseById: Should throw SEMESTER_NOT_FOUND when ID is not found")
    void getResponseById_NotFound() {
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> semesterService.getResponseById(semesterId))
                .withMessage("SEMESTER_NOT_FOUND");
    }

    @Test
    @DisplayName("getById: Should return Semester when ID is found")
    void getById_Found() {
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(mockSemester));

        Semester result = semesterService.getById(semesterId);

        assertThat(result).isEqualTo(mockSemester);
    }

    @Test
    @DisplayName("getById: Should return null when ID is not found")
    void getById_NotFound() {
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.empty());

        Semester result = semesterService.getById(semesterId);

        assertThat(result).isNull();
    }

    // --- create(CreateSemesterRequest) Tests ---

    @Test
    @DisplayName("create(Request): Should successfully create a semester and return response")
    void createFromRequest_Success() {
        // Arrange
        CreateSemesterRequest request = new CreateSemesterRequest("Spring 2026", LocalDate.now().plusYears(1), LocalDate.now().plusYears(1).plusMonths(4));
        Semester semesterToSave = new Semester(); // Mock mapping result
        Semester savedSemester = new Semester();
        savedSemester.setId(UUID.randomUUID());
        SemesterResponse expectedResponse = new SemesterResponse();

        when(modelMapper.map(request, Semester.class)).thenReturn(semesterToSave);
        // Mock the internal call to create(Semester)
        SemesterService spyService = spy(semesterService);
        doReturn(savedSemester).when(spyService).create(semesterToSave);
        when(modelMapper.map(savedSemester, SemesterResponse.class)).thenReturn(expectedResponse);

        // Act
        SemesterResponse result = spyService.create(request);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
    }

    // --- create(Semester) Tests ---

    @Test
    @DisplayName("create(String, Date, Date): Should map to Semester and call create(Semester)")
    void createWithPrimitives_Success() {
        // Arrange
        String name = "Summer 2026";
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 8, 30);

        Semester createdSemester = new Semester();
        createdSemester.setName(name);
        createdSemester.setStartDate(start);
        createdSemester.setEndDate(end);

        // Mock the internal create(Semester) call
        SemesterService spyService = spy(semesterService);
        doReturn(mockSemester).when(spyService).create(any(Semester.class));

        // Act
        Semester result = spyService.create(name, start, end);

        // Assert
        assertThat(result).isEqualTo(mockSemester);
        verify(spyService, times(1)).create(any(Semester.class));
    }

    // --- create(String, LocalDate, LocalDate) Tests ---

    @Test
    @DisplayName("update: Should successfully update semester and return response")
    void update_Success() {
        // Arrange
        UpdateSemesterRequest request = new UpdateSemesterRequest("Updated Fall 2025", LocalDate.of(2025, 9, 5), LocalDate.of(2025, 12, 25));
        Semester updatedSemester = new Semester();
        updatedSemester.setId(semesterId);
        SemesterResponse expectedResponse = new SemesterResponse();

        when(semesterRepository.existsById(semesterId)).thenReturn(true);
        when(modelMapper.map(request, Semester.class)).thenReturn(updatedSemester);
        when(semesterRepository.save(updatedSemester)).thenReturn(updatedSemester);
        when(modelMapper.map(updatedSemester, SemesterResponse.class)).thenReturn(expectedResponse);

        // Act
        SemesterResponse result = semesterService.update(semesterId, request);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        verify(updatedSemester).setId(semesterId); // Ensure ID is set on mapped object
        verify(semesterRepository, times(1)).save(updatedSemester);
    }

    // --- update Tests ---

    @Test
    @DisplayName("update: Should throw SEMESTER_NOT_FOUND when ID doesn't exist")
    void update_NotFound() {
        // Arrange
        UpdateSemesterRequest request = new UpdateSemesterRequest();
        when(semesterRepository.existsById(semesterId)).thenReturn(false);

        // Act & Assert
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> semesterService.update(semesterId, request))
                .withMessage("SEMESTER_NOT_FOUND");
        verify(semesterRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete: Should delete semester and return response")
    void delete_Success() {
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(mockSemester));
        when(modelMapper.map(mockSemester, SemesterResponse.class)).thenReturn(mockSemesterResponse);
        doNothing().when(semesterRepository).delete(mockSemester);

        SemesterResponse result = semesterService.delete(semesterId);

        assertThat(result).isEqualTo(mockSemesterResponse);
        verify(semesterRepository, times(1)).delete(mockSemester);
    }

    // --- delete Tests ---

    @Test
    @DisplayName("delete: Should throw SEMESTER_NOT_FOUND when ID doesn't exist")
    void delete_NotFound() {
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> semesterService.delete(semesterId))
                .withMessage("SEMESTER_NOT_FOUND");
        verify(semesterRepository, never()).delete(any(Semester.class));
    }

    @Nested
    @DisplayName("create(Semester) Validation Tests")
    class CreateSemesterValidationTests {

        private final LocalDate start = LocalDate.of(2026, 1, 1);
        private final LocalDate end = LocalDate.of(2026, 4, 30);

        @BeforeEach
        void setup() {
            // Default mock: No name conflict, no overlapping dates
            when(semesterRepository.exists(any(Specification.class))).thenReturn(false);
            when(semesterRepository.save(any(Semester.class))).thenReturn(mockSemester);
        }

        @Test
        @DisplayName("create(Semester): Should successfully save semester if no conflicts")
        void createSemester_Success() {
            // Arrange
            Semester newSemester = new Semester();
            newSemester.setName("New Semester");
            newSemester.setStartDate(start);
            newSemester.setEndDate(end);

            // Act
            Semester result = semesterService.create(newSemester);

            // Assert
            assertThat(result).isEqualTo(mockSemester);
            verify(semesterRepository, times(1)).save(newSemester);
        }

        @Test
        @DisplayName("create(Semester): Should throw SEMESTER_NAME_EXISTED if name conflict")
        void createSemester_NameExisted() {
            // Arrange
            Semester newSemester = new Semester();
            newSemester.setName("Fall 2025");

            // Mock name existence check
            when(semesterRepository.exists((r, q, c) -> c.equal(r.get("name"), "Fall 2025")))
                    .thenReturn(true);

            // Act & Assert
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> semesterService.create(newSemester))
                    .withMessage("SEMESTER_NAME_EXISTED");
            verify(semesterRepository, never()).save(any());
        }

        @Test
        @DisplayName("create(Semester): Should throw SEMESTER_OVERLAPPING if dates overlap")
        void createSemester_Overlapping() {
            // Arrange
            Semester newSemester = new Semester();
            newSemester.setName("New Semester");
            newSemester.setStartDate(start.minusDays(5));
            newSemester.setEndDate(end.minusDays(5));

            // Mock date overlapping check
            when(semesterRepository.exists(any(Specification.class)))
                    .thenReturn(true);

            // Act & Assert
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> semesterService.create(newSemester))
                    .withMessage("SEMESTER_OVERLAPPING");
            verify(semesterRepository, never()).save(any());
        }
    }
}