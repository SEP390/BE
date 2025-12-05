package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.ew.CreateEWRoomRequest;
import com.capstone.capstone.dto.response.ew.EWRoomResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.EWRoomRepository;
import com.capstone.capstone.repository.EWUsageRepository;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.repository.SemesterRepository;
import com.capstone.capstone.util.SpecQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EWRoomServiceTest {
    private final UUID ROOM_ID = UUID.randomUUID();
    private final UUID USER_ID_1 = UUID.randomUUID();
    private final UUID USER_ID_2 = UUID.randomUUID();
    private final LocalDate TODAY = LocalDate.now();
    private final LocalDate LAST_MONTH = TODAY.minusMonths(1);
    private final LocalDate CURRENT_SEMESTER_START = TODAY.minusDays(15);
    private final LocalDate PREVIOUS_SEMESTER_START = TODAY.minusMonths(3);
    @Mock
    private EWRoomRepository ewRoomRepository;
    @Mock
    private EWUsageRepository ewUsageRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private SemesterService semesterService;
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private SpecQuery<EWRoom> specQuery;
    @InjectMocks
    private EWRoomService ewRoomService;
    private Room mockRoom;
    private User mockUser1;
    private User mockUser2;
    private Semester currentSemester;
    private Semester previousSemester;

    @BeforeEach
    void setUp() {
        mockRoom = new Room();
        mockRoom.setId(ROOM_ID);
        mockUser1 = new User();
        mockUser1.setId(USER_ID_1);
        mockUser2 = new User();
        mockUser2.setId(USER_ID_2);
        currentSemester = new Semester();
        currentSemester.setId(UUID.randomUUID());
        currentSemester.setStartDate(CURRENT_SEMESTER_START);
        previousSemester = new Semester();
        previousSemester.setId(UUID.randomUUID());
        previousSemester.setStartDate(PREVIOUS_SEMESTER_START);
    }

    private EWRoom createEWRoom(int electric, int water, LocalDate createDate) {
        EWRoom ewRoom = new EWRoom();
        ewRoom.setElectric(electric);
        ewRoom.setWater(water);
        ewRoom.setCreateDate(createDate);
        return ewRoom;
    }

    private CreateEWRoomRequest createEWRoomRequest(UUID roomId, int electric, int water) {
        CreateEWRoomRequest req = new CreateEWRoomRequest();
        req.setRoomId(roomId);
        req.setElectric(electric);
        req.setWater(water);
        return req;
    }

    private void mockCommonDependencies(boolean hasPreviousSemester) {
        lenient().when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(mockRoom));
        lenient().when(roomRepository.findUsers(mockRoom)).thenReturn(List.of(mockUser1, mockUser2));
        lenient().when(semesterService.getCurrent()).thenReturn(Optional.of(currentSemester));
        if (hasPreviousSemester) {
            lenient().when(semesterRepository.findPrevious()).thenReturn(Optional.of(previousSemester));
        } else {
            lenient().when(semesterRepository.findPrevious()).thenReturn(Optional.empty());
        }
        lenient().when(ewRoomRepository.save(any(EWRoom.class))).thenAnswer(invocation -> {
            EWRoom saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        lenient().when(modelMapper.map(any(EWRoom.class), eq(EWRoomResponse.class))).thenReturn(new EWRoomResponse());
    }

    @Test
    void create_ShouldThrowException_WhenRoomNotFound() {
        CreateEWRoomRequest request = createEWRoomRequest(ROOM_ID, 100, 50);
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ewRoomService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessage("ROOM_NOT_FOUND");
    }

    @Test
    void create_ShouldThrowException_WhenCurrentSemesterNotFound() {
        CreateEWRoomRequest request = createEWRoomRequest(ROOM_ID, 100, 50);
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(mockRoom));
        when(semesterService.getCurrent()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ewRoomService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessage("CURRENT_SEMESTER_NOT_FOUND");
    }

    @Test
    void create_ShouldThrowException_WhenPreviousSemesterNotFound() {
        CreateEWRoomRequest request = createEWRoomRequest(ROOM_ID, 100, 50);
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(mockRoom));
        when(semesterService.getCurrent()).thenReturn(Optional.of(currentSemester));
        when(semesterRepository.findPrevious()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ewRoomService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessage("PREVIOUS_SEMESTER_NOT_FOUND");
    }

    @Test
    void create_ShouldThrowException_WhenAlreadyCreatedToday() {
        mockCommonDependencies(true);
        CreateEWRoomRequest request = createEWRoomRequest(ROOM_ID, 100, 50);
        EWRoom recentEWRoom = createEWRoom(50, 20, TODAY);
        when(ewRoomRepository.findRecent(eq(mockRoom), eq(currentSemester))).thenReturn(Optional.of(recentEWRoom));
        when(ewRoomRepository.findRecent(eq(mockRoom), eq(previousSemester))).thenReturn(Optional.of(createEWRoom(20, 10, LAST_MONTH.minusDays(10))));
        assertThatThrownBy(() -> ewRoomService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessage("ALREADY_CREATE");
    }

    @Test
    void create_Case1_Success_RecentInCurrentSemester() {
        mockCommonDependencies(true);
        CreateEWRoomRequest request = createEWRoomRequest(ROOM_ID, 100, 50);
        LocalDate previousReadingDate = LAST_MONTH;
        EWRoom recentInCurrent = createEWRoom(50, 20, previousReadingDate);
        when(ewRoomRepository.findRecent(eq(mockRoom), eq(currentSemester))).thenReturn(Optional.of(recentInCurrent));
        when(ewRoomRepository.findRecent(eq(mockRoom), eq(previousSemester))).thenReturn(Optional.of(createEWRoom(10, 5, PREVIOUS_SEMESTER_START)));
        int expectedElectricUsed = 50;
        int expectedWaterUsed = 30;
        LocalDate expectedStartDate = previousReadingDate;
        ewRoomService.create(request);
        ArgumentCaptor<EWRoom> ewRoomCaptor = ArgumentCaptor.forClass(EWRoom.class);
        verify(ewRoomRepository).save(ewRoomCaptor.capture());
        EWRoom savedEWRoom = ewRoomCaptor.getValue();
        assertThat(savedEWRoom.getElectricUsed()).isEqualTo(expectedElectricUsed);
        assertThat(savedEWRoom.getWaterUsed()).isEqualTo(expectedWaterUsed);
        assertThat(savedEWRoom.getCreateDate()).isEqualTo(TODAY);
        ArgumentCaptor<EWUsage> ewUsageCaptor = ArgumentCaptor.forClass(EWUsage.class);
        verify(ewUsageRepository, times(2)).save(ewUsageCaptor.capture());
        List<EWUsage> savedUsages = ewUsageCaptor.getAllValues();
        EWUsage usage1 = savedUsages.get(0);
        assertThat(usage1.getUser()).isEqualTo(mockUser1);
        assertThat(usage1.getElectric()).isEqualTo(expectedElectricUsed);
        assertThat(usage1.getWater()).isEqualTo(expectedWaterUsed);
        assertThat(usage1.getStartDate()).isEqualTo(expectedStartDate);
        assertThat(usage1.getEndDate()).isEqualTo(TODAY);
        assertThat(usage1.getEwRoom()).isEqualTo(savedEWRoom);
    }

    @Test
    void create_Case2_Success_FirstOfMonth_RecentInPreviousSemester() {
        mockCommonDependencies(true);
        CreateEWRoomRequest request = createEWRoomRequest(ROOM_ID, 100, 50);
        when(ewRoomRepository.findRecent(eq(mockRoom), eq(currentSemester))).thenReturn(Optional.empty());
        EWRoom recentInPrevious = createEWRoom(80, 40, PREVIOUS_SEMESTER_START.plusMonths(2));
        when(ewRoomRepository.findRecent(eq(mockRoom), eq(previousSemester))).thenReturn(Optional.of(recentInPrevious));
        int expectedElectricUsed = 20;
        int expectedWaterUsed = 10;
        LocalDate expectedStartDate = CURRENT_SEMESTER_START;
        ewRoomService.create(request);
        ArgumentCaptor<EWRoom> ewRoomCaptor = ArgumentCaptor.forClass(EWRoom.class);
        verify(ewRoomRepository).save(ewRoomCaptor.capture());
        EWRoom savedEWRoom = ewRoomCaptor.getValue();
        assertThat(savedEWRoom.getElectricUsed()).isEqualTo(expectedElectricUsed);
        assertThat(savedEWRoom.getWaterUsed()).isEqualTo(expectedWaterUsed);
        ArgumentCaptor<EWUsage> ewUsageCaptor = ArgumentCaptor.forClass(EWUsage.class);
        verify(ewUsageRepository, times(2)).save(ewUsageCaptor.capture());
        EWUsage usage1 = ewUsageCaptor.getAllValues().get(0);
        assertThat(usage1.getElectric()).isEqualTo(expectedElectricUsed);
        assertThat(usage1.getWater()).isEqualTo(expectedWaterUsed);
        assertThat(usage1.getStartDate()).isEqualTo(expectedStartDate);
    }

    @Test
    void create_Case3_Success_FirstEverRecord() {
        mockCommonDependencies(true);
        CreateEWRoomRequest request = createEWRoomRequest(ROOM_ID, 100, 50);
        when(ewRoomRepository.findRecent(eq(mockRoom), eq(currentSemester))).thenReturn(Optional.empty());
        when(ewRoomRepository.findRecent(eq(mockRoom), eq(previousSemester))).thenReturn(Optional.empty());
        int expectedElectricUsed = 100;
        int expectedWaterUsed = 50;
        LocalDate expectedStartDate = CURRENT_SEMESTER_START;
        ewRoomService.create(request);
        ArgumentCaptor<EWRoom> ewRoomCaptor = ArgumentCaptor.forClass(EWRoom.class);
        verify(ewRoomRepository).save(ewRoomCaptor.capture());
        EWRoom savedEWRoom = ewRoomCaptor.getValue();
        assertThat(savedEWRoom.getElectricUsed()).isEqualTo(expectedElectricUsed);
        assertThat(savedEWRoom.getWaterUsed()).isEqualTo(expectedWaterUsed);
        ArgumentCaptor<EWUsage> ewUsageCaptor = ArgumentCaptor.forClass(EWUsage.class);
        verify(ewUsageRepository, times(2)).save(ewUsageCaptor.capture());
        EWUsage usage1 = ewUsageCaptor.getAllValues().get(0);
        assertThat(usage1.getElectric()).isEqualTo(expectedElectricUsed);
        assertThat(usage1.getWater()).isEqualTo(expectedWaterUsed);
        assertThat(usage1.getStartDate()).isEqualTo(expectedStartDate);
    }

    @Test
    void create_ShouldThrowException_WhenNegativeElectricUsed() {
        mockCommonDependencies(true);
        CreateEWRoomRequest request = createEWRoomRequest(ROOM_ID, 100, 50);
        LocalDate previousReadingDate = LAST_MONTH;
        EWRoom recentInCurrent = createEWRoom(150, 20, previousReadingDate);
        when(ewRoomRepository.findRecent(eq(mockRoom), eq(currentSemester))).thenReturn(Optional.of(recentInCurrent));
        assertThatThrownBy(() -> ewRoomService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessage("ELECTRIC_USED_NEGATIVE");
    }

    @Test
    void create_ShouldThrowException_WhenNegativeWaterUsed() {
        mockCommonDependencies(true);
        CreateEWRoomRequest request = createEWRoomRequest(ROOM_ID, 100, 50);
        LocalDate previousReadingDate = LAST_MONTH;
        EWRoom recentInCurrent = createEWRoom(50, 70, previousReadingDate);
        when(ewRoomRepository.findRecent(eq(mockRoom), eq(currentSemester))).thenReturn(Optional.of(recentInCurrent));
        assertThatThrownBy(() -> ewRoomService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessage("WATER_USED_NEGATIVE");
    }

    @Test
    void getAll_ShouldReturnPagedModel_AndApplyFiltersCorrectly() {
        Map<String, Object> filter = Map.of("roomId", ROOM_ID.toString(), "semesterId", UUID.randomUUID().toString(), "startDate", LocalDate.of(2023, 1, 1), "endDate", LocalDate.of(2023, 1, 31));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        List<EWRoom> ewRoomList = List.of(new EWRoom(), new EWRoom());
        Page<EWRoom> ewRoomPage = new PageImpl<>(ewRoomList, pageable, 2);
        when(ewRoomRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(ewRoomPage);
        EWRoomResponse response1 = new EWRoomResponse();
        EWRoomResponse response2 = new EWRoomResponse();
        when(modelMapper.map(eq(ewRoomList.get(0)), eq(EWRoomResponse.class))).thenReturn(response1);
        when(modelMapper.map(eq(ewRoomList.get(1)), eq(EWRoomResponse.class))).thenReturn(response2);
        PagedModel<EWRoomResponse> result = ewRoomService.getAll(filter, pageable);
        verify(ewRoomRepository).findAll(any(Specification.class), eq(pageable));
        verify(modelMapper, times(2)).map(any(EWRoom.class), eq(EWRoomResponse.class));
        assertThat(result.getContent()).hasSize(2).containsExactly(response1, response2);
        assertThat(result.getMetadata().totalElements()).isEqualTo(2);
        assertThat(result.getMetadata().number()).isEqualTo(0);
        assertThat(result.getMetadata().size()).isEqualTo(10);
    }

    @Test
    void getAll_ShouldReturnEmptyPagedModel_WhenNoData() {
        Map<String, Object> filter = Collections.emptyMap();
        Pageable pageable = PageRequest.of(0, 10);
        Page<EWRoom> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(ewRoomRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
        PagedModel<EWRoomResponse> result = ewRoomService.getAll(filter, pageable);
        verify(ewRoomRepository).findAll(any(Specification.class), eq(pageable));
        verify(modelMapper, never()).map(any(), any());
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getMetadata().totalElements()).isEqualTo(0);
    }
}