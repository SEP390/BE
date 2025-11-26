package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.request.room.UpdateRoomRequest;
import com.capstone.capstone.dto.response.booking.UserMatching;
import com.capstone.capstone.dto.response.room.*;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.SecurityUtils;
import com.capstone.capstone.util.SortUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    private final UUID roomId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private RoomPricingService roomPricingService;
    @Mock
    private SurveyQuestionRepository surveyQuestionRepository;
    @Mock
    private MatchingRepository matchingRepository;
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private SlotService slotService;
    @Mock
    private SlotHistoryService slotHistoryService;
    @Mock
    private SemesterService semesterService;
    @Mock
    private SurveySelectRepository surveySelectRepository;
    @Mock
    private TimeConfigService timeConfigService;
    @InjectMocks
    private RoomService roomService;
    private Room mockRoom;
    private User mockUser;
    private Dorm mockDorm;
    private RoomResponseJoinPricingAndDormAndSlot mockRoomResponse;
    private RoomPricing mockRoomPricing;
    private TimeConfig mockTimeConfig;
    private Semester mockSemester;
    private Pageable pageable;

    /**
     * Setup method to initialize common objects before each test.
     */
    @BeforeEach
    void setUp() {
        mockDorm = new Dorm();
        mockDorm.setId(UUID.randomUUID());
        mockDorm.setTotalFloor(5);
        mockRoom = new Room();
        mockRoom.setId(roomId);
        mockRoom.setRoomNumber("R101");
        mockRoom.setTotalSlot(2);
        mockRoom.setDorm(mockDorm);
        mockRoom.setFloor(1);
        mockRoom.setStatus(StatusRoomEnum.AVAILABLE);
        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setGender(GenderEnum.MALE);
        mockRoomPricing = new RoomPricing();
        mockRoomPricing.setId(UUID.randomUUID());
        mockRoomPricing.setTotalSlot(2);
        mockRoomResponse = new RoomResponseJoinPricingAndDormAndSlot();
        mockRoomResponse.setId(roomId);
        mockRoomResponse.setRoomNumber("R101");
        mockTimeConfig = new TimeConfig();
        mockTimeConfig.setStartBookingDate(LocalDate.now().minusDays(1));
        mockTimeConfig.setEndBookingDate(LocalDate.now().plusDays(1));
        mockTimeConfig.setStartExtendDate(LocalDate.now().minusDays(1));
        mockTimeConfig.setEndExtendDate(LocalDate.now().plusDays(1));
        mockSemester = new Semester();
        mockSemester.setId(UUID.randomUUID());
        pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
    }

    @Test
    @DisplayName("getResponseById: Should return DTO when ID is found")
    void getResponseById_Found() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(modelMapper.map(any(Room.class), any(Class.class))).thenReturn(mockRoomResponse);
        RoomResponseJoinPricingAndDormAndSlot actualResponse = roomService.getResponseById(roomId);
        assertThat(actualResponse).isNotNull().isEqualTo(mockRoomResponse);
    }

    @Test
    @DisplayName("getResponseById: Should throw AppException when ID is not found")
    void getResponseById_NotFound() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> roomService.getResponseById(roomId))
                .withMessage("ROOM_NOT_FOUND");
    }

    @Test
    @DisplayName("getById: Should return Optional<Room> when ID is found")
    void getById_Found() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));
        Optional<Room> result = roomService.getById(roomId);
        assertThat(result).isPresent().contains(mockRoom);
    }

    @Test
    @DisplayName("checkFullAndUpdate: Should set status to FULL if room is full")
    void checkFullAndUpdate_IsFull() {
        when(roomRepository.isFull(mockRoom)).thenReturn(true);
        when(roomRepository.save(mockRoom)).thenReturn(mockRoom);
        roomService.checkFullAndUpdate(mockRoom);
        assertThat(mockRoom.getStatus()).isEqualTo(StatusRoomEnum.FULL);
        verify(roomRepository, times(1)).save(mockRoom);
    }

    @Test
    @DisplayName("checkFullAndUpdate: Should set status to AVAILABLE if room is not full")
    void checkFullAndUpdate_IsNotFull() {
        mockRoom.setStatus(StatusRoomEnum.FULL);
        when(roomRepository.isFull(mockRoom)).thenReturn(false);
        when(roomRepository.save(mockRoom)).thenReturn(mockRoom);
        roomService.checkFullAndUpdate(mockRoom);
        assertThat(mockRoom.getStatus()).isEqualTo(StatusRoomEnum.AVAILABLE);
        verify(roomRepository, times(1)).save(mockRoom);
    }

    @Test
    @DisplayName("get: Should return PagedModel of rooms with filtering")
    void get_Success() {
        Page<Room> roomPage = new PageImpl<>(List.of(mockRoom));
        when(roomRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(roomPage);
        when(modelMapper.map(any(Room.class), any(Class.class))).thenReturn(mockRoomResponse);
        try (MockedStatic<SortUtil> mockedSortUtil = mockStatic(SortUtil.class)) {
            mockedSortUtil.when(() -> SortUtil.getSort(any(Pageable.class), any())).thenReturn(Sort.by("id"));
            Map<String, Object> filter = Map.of("dormId", mockDorm.getId().toString());
            PagedModel<RoomResponseJoinPricingAndDormAndSlot> result = roomService.get(filter, pageable);
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(roomRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("create(Room): Should create room successfully with defaults")
    void create_Room_SuccessWithDefaults() {
        Room roomToCreate = new Room();
        roomToCreate.setRoomNumber("R202");
        roomToCreate.setDorm(mockDorm);
        when(roomRepository.exists(any(Specification.class))).thenReturn(false);
        when(roomPricingService.getOrCreate(anyInt())).thenReturn(mockRoomPricing);
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> {
            Room savedRoom = i.getArgument(0);
            savedRoom.setId(roomId);
            return savedRoom;
        });
        when(slotService.create(any(Room.class))).thenReturn(List.of(new Slot()));
        Room result = roomService.create(roomToCreate);
        assertThat(result.getTotalSlot()).isEqualTo(2);
        assertThat(result.getFloor()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(StatusRoomEnum.AVAILABLE);
        assertThat(result.getPricing()).isEqualTo(mockRoomPricing);
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(slotService, times(1)).create(any(Room.class));
    }

    @Test
    @DisplayName("create(Room): Should throw DORM_NULL when dorm is null")
    void create_Room_DormNull() {
        Room roomToCreate = new Room();
        roomToCreate.setRoomNumber("R202");
        roomToCreate.setDorm(null);
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> roomService.create(roomToCreate))
                .withMessage("DORM_NULL");
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("create(Room): Should throw ROOM_NUMBER_EXISTED when room number already exists in dorm")
    void create_Room_RoomNumberExisted() {
        Room roomToCreate = new Room();
        roomToCreate.setRoomNumber("R101");
        roomToCreate.setDorm(mockDorm);
        when(roomRepository.exists(any(Specification.class))).thenReturn(true);
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> roomService.create(roomToCreate))
                .withMessage("ROOM_NUMBER_EXISTED");
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("create(List<Room>): Should create list of rooms successfully")
    void create_ListRoom_Success() {
        Room room1 = new Room();
        room1.setTotalSlot(2);
        Room room2 = new Room();
        room2.setTotalSlot(4);
        List<Room> roomsToCreate = Arrays.asList(room1, room2);
        when(roomPricingService.getByTotalSlot(2)).thenReturn(Optional.of(mockRoomPricing));
        when(roomPricingService.getByTotalSlot(4)).thenReturn(Optional.of(mockRoomPricing));
        when(roomRepository.saveAll(roomsToCreate)).thenReturn(roomsToCreate);
        List<Room> result = roomService.create(roomsToCreate);
        assertThat(result).hasSize(2);
        verify(roomRepository, times(1)).saveAll(roomsToCreate);
        verify(slotService, times(2)).create(any(Room.class));
        assertThat(room1.getPricing()).isEqualTo(mockRoomPricing);
        assertThat(room2.getPricing()).isEqualTo(mockRoomPricing);
    }

    @Test
    @DisplayName("create(List<Room>): Should throw ROOM_TYPE_NOT_EXIST if pricing not found")
    void create_ListRoom_PricingNotExist() {
        Room room1 = new Room();
        room1.setTotalSlot(99);
        List<Room> roomsToCreate = List.of(room1);
        when(roomPricingService.getByTotalSlot(99)).thenReturn(Optional.empty());
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> roomService.create(roomsToCreate))
                .withMessage("ROOM_TYPE_NOT_EXIST");
        verify(roomRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("update(UUID, UpdateRoomRequest): Should map request and call update(Room)")
    void update_ByIdAndRequest_Success() {
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setTotalSlot(4);
        request.setFloor(2);
        request.setRoomNumber("R202");
        request.setStatus(StatusRoomEnum.AVAILABLE);
        Room oldRoom = new Room();
        oldRoom.setId(roomId);
        oldRoom.setDorm(mockDorm);
        oldRoom.setTotalSlot(2);
        oldRoom.setRoomNumber("R101");
        Room updatedRoom = new Room();
        updatedRoom.setId(roomId);
        updatedRoom.setTotalSlot(4);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(oldRoom));
        RoomService spyService = spy(roomService);
        doReturn(updatedRoom).when(spyService).update(any(Room.class));
        when(modelMapper.map(updatedRoom, RoomResponseJoinPricingAndDormAndSlot.class)).thenReturn(mockRoomResponse);
        RoomResponseJoinPricingAndDormAndSlot result = spyService.update(roomId, request);
        assertThat(result).isEqualTo(mockRoomResponse);
        verify(spyService, times(1)).update(argThat(room ->
                room.getTotalSlot() == 4 && room.getFloor() == 2 && room.getRoomNumber().equals("R202")
        ));
    }

    @Test
    @DisplayName("update(UUID, UpdateRoomRequest): Should throw ROOM_NOT_FOUND")
    void update_ByIdAndRequest_NotFound() {
        UpdateRoomRequest request = new UpdateRoomRequest();
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> roomService.update(roomId, request))
                .withMessage("ROOM_NOT_FOUND");
    }

    @Test
    @DisplayName("getUsersResponse: Should return list of RoomUserResponse for users in room")
    void getUsersResponse_Success() {
        Slot slot1 = new Slot();
        slot1.setUser(mockUser);
        Slot slot2 = new Slot();
        slot2.setUser(null);
        mockRoom.setSlots(Arrays.asList(slot1, slot2));
        RoomUserResponse userResponse = new RoomUserResponse();
        userResponse.setId(mockUser.getId());
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(modelMapper.map(mockUser, RoomUserResponse.class)).thenReturn(userResponse);
        List<RoomUserResponse> result = roomService.getUsersResponse(roomId);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(mockUser.getId());
        verify(modelMapper, times(1)).map(mockUser, RoomUserResponse.class);
    }

    @Test
    @DisplayName("getUsersResponse: Should throw ROOM_NOT_FOUND when room ID is invalid")
    void getUsersResponse_NotFound() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> roomService.getUsersResponse(roomId))
                .withMessage("ROOM_NOT_FOUND");
    }

    @Test
    @DisplayName("getUsers: Should return list of users for a room")
    void getUsers_Success() {
        List<User> expectedUsers = List.of(mockUser);
        when(roomRepository.findUsers(mockRoom)).thenReturn(expectedUsers);
        List<User> result = roomService.getUsers(mockRoom);
        assertThat(result).isEqualTo(expectedUsers);
    }

    @Test
    @DisplayName("getAllByDorm: Should return list of rooms for a dorm")
    void getAllByDorm_Success() {
        List<Room> expectedRooms = List.of(mockRoom);
        when(roomRepository.findAll(any(Specification.class))).thenReturn(expectedRooms);
        List<Room> result = roomService.getAllByDorm(mockDorm);
        assertThat(result).isEqualTo(expectedRooms);
        verify(roomRepository, times(1)).findAll(any(Specification.class));
    }

    @Nested
    @DisplayName("getMatching Tests")
    class GetMatchingTests {
        private MockedStatic<SecurityUtils> mockedSecurityUtils;

        @BeforeEach
        void setup() {
            mockedSecurityUtils = mockStatic(SecurityUtils.class);
            mockedSecurityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            when(semesterService.getNext()).thenReturn(mockSemester);
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(true);
            when(surveyQuestionRepository.count()).thenReturn(10L);
            when(roomRepository.findAvailableForGender(any(GenderEnum.class))).thenReturn(Arrays.asList(mockRoom));
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(false);
        }

        @Test
        @DisplayName("Should return a list of matching rooms successfully for first-time booker")
        void getMatching_SuccessFirstTime() {
            RoomMatching mockMatchingResult = new RoomMatching() {
                @Override
                public UUID getRoomId() {
                    return roomId;
                }

                @Override
                public Integer getSameOptionCount() {
                    return 5;
                }

                @Override
                public Integer getUserCount() {
                    return 1;
                }
            };
            when(matchingRepository.computeRoomMatching(mockUser, Arrays.asList(mockRoom)))
                    .thenReturn(Arrays.asList(mockMatchingResult));
            when(modelMapper.map(any(Room.class), any(Class.class))).thenReturn(new RoomMatchingResponse());
            List<RoomMatchingResponse> result = roomService.getMatching();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMatching()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should throw BOOKING_DATE_NOT_START for first-time booker outside booking dates")
        void getMatching_BookingDateNotStart() {
            mockTimeConfig.setEndBookingDate(LocalDate.now().minusDays(1));
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.getMatching())
                    .withMessage("BOOKING_DATE_NOT_START");
        }

        @Test
        @DisplayName("Should throw BOOKING_DATE_NOT_START for existing user outside extend dates")
        void getMatching_ExtendDateNotStart() {
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(true);
            mockTimeConfig.setEndExtendDate(LocalDate.now().minusDays(1));
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.getMatching())
                    .withMessage("BOOKING_DATE_NOT_START");
        }

        @Test
        @DisplayName("Should throw NEXT_SEMESTER_NOT_FOUND")
        void getMatching_NextSemesterNotFound() {
            when(semesterService.getNext()).thenReturn(null);
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.getMatching())
                    .withMessage("NEXT_SEMESTER_NOT_FOUND");
        }

        @Test
        @DisplayName("Should throw SURVEY_NOT_FOUND")
        void getMatching_SurveyNotFound() {
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(false);
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.getMatching())
                    .withMessage("SURVEY_NOT_FOUND");
        }

        @Test
        @DisplayName("Should handle TIME_CONFIG_NOT_FOUND")
        void getMatching_TimeConfigNotFound() {
            when(timeConfigService.getCurrent()).thenReturn(Optional.empty());
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.getMatching())
                    .withMessage("TIME_CONFIG_NOT_FOUND");
        }

        @Test
        @DisplayName("Should limit results to 5 rooms")
        void getMatching_LimitResults() {
            List<Room> manyRooms = new ArrayList<>();
            List<RoomMatching> matchingResults = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                Room room = new Room();
                room.setId(UUID.randomUUID());
                manyRooms.add(room);
                matchingResults.add(new RoomMatching() {
                    @Override
                    public UUID getRoomId() {
                        return room.getId();
                    }

                    @Override
                    public Integer getSameOptionCount() {
                        return 5;
                    }

                    @Override
                    public Integer getUserCount() {
                        return 1;
                    }
                });
            }
            when(roomRepository.findAvailableForGender(any(GenderEnum.class))).thenReturn(manyRooms);
            when(matchingRepository.computeRoomMatching(mockUser, manyRooms)).thenReturn(matchingResults);
            when(modelMapper.map(any(Room.class), any(Class.class))).thenReturn(new RoomMatchingResponse());
            List<RoomMatchingResponse> result = roomService.getMatching();
            assertThat(result).hasSize(5);
        }

        @AfterEach
        void tearDown() {
            mockedSecurityUtils.close();
        }
    }

    @Nested
    @DisplayName("getRoommates Tests")
    class GetRoommatesTests {
        private MockedStatic<SecurityUtils> mockedSecurityUtils;

        @BeforeEach
        void setup() {
            mockedSecurityUtils = mockStatic(SecurityUtils.class);
            mockedSecurityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        }

        @Test
        @DisplayName("Should return a list of roommates with matching percentage")
        void getRoommates_Success() {
            Slot mockSlot = new Slot();
            mockSlot.setRoom(mockRoom);
            mockUser.setSlot(mockSlot);
            User roommate1 = new User();
            roommate1.setId(UUID.randomUUID());
            User roommate2 = new User();
            roommate2.setId(UUID.randomUUID());
            List<User> roommates = Arrays.asList(mockUser, roommate1, roommate2);
            when(roomRepository.findUsers(mockRoom)).thenReturn(roommates);
            when(surveyQuestionRepository.count()).thenReturn(10L);
            when(matchingRepository.computeUserMatching(eq(mockUser), anyList()))
                    .thenReturn(Arrays.asList(
                            new UserMatching() {
                                @Override
                                public UUID getId() {
                                    return roommate1.getId();
                                }

                                @Override
                                public Double getSameOptionCount() {
                                    return 8.0;
                                }
                            },
                            new UserMatching() {
                                @Override
                                public UUID getId() {
                                    return roommate2.getId();
                                }

                                @Override
                                public Double getSameOptionCount() {
                                    return 2.0;
                                }
                            }
                    ));
            RoommateResponse res1 = new RoommateResponse();
            res1.setId(roommate1.getId());
            res1.setMatching(80.0);
            RoommateResponse res2 = new RoommateResponse();
            res2.setId(roommate2.getId());
            res2.setMatching(20.0);
            when(modelMapper.map(eq(roommate1), any(Class.class))).thenReturn(res1);
            when(modelMapper.map(eq(roommate2), any(Class.class))).thenReturn(res2);
            List<RoommateResponse> result = roomService.getRoommates();
            assertThat(result).hasSize(2);
            assertThat(result.stream().map(RoommateResponse::getId).collect(Collectors.toList()))
                    .doesNotContain(userId);
            assertThat(result.stream().filter(r -> r.getId().equals(roommate1.getId())).findFirst().get().getMatching())
                    .isEqualTo(80.0);
        }

        @Test
        @DisplayName("Should throw SLOT_NOT_FOUND when user has no slot")
        void getRoommates_SlotNotFound() {
            mockUser.setSlot(null);
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.getRoommates())
                    .withMessage("SLOT_NOT_FOUND");
        }

        @AfterEach
        void tearDown() {
            mockedSecurityUtils.close();
        }
    }

    @Nested
    @DisplayName("getBooking Tests")
    class GetBookingTests {
        private MockedStatic<SecurityUtils> mockedSecurityUtils;

        @BeforeEach
        void setup() {
            mockedSecurityUtils = mockStatic(SecurityUtils.class);
            mockedSecurityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            when(semesterService.getNext()).thenReturn(mockSemester);
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(true);
            when(roomRepository.findAvailableForGender(any(GenderEnum.class))).thenReturn(Arrays.asList(mockRoom));
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(false);
        }

        @Test
        @DisplayName("getBooking: Should return PagedModel of available rooms with filtering")
        void getBooking_SuccessWithFilter() {
            Page<Room> roomPage = new PageImpl<>(List.of(mockRoom));
            when(roomRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(roomPage);
            when(modelMapper.map(any(Room.class), any(Class.class))).thenReturn(mockRoomResponse);
            try (MockedStatic<SortUtil> mockedSortUtil = mockStatic(SortUtil.class)) {
                mockedSortUtil.when(() -> SortUtil.getSort(any(Pageable.class), any())).thenReturn(Sort.by("id"));
                PagedModel<RoomResponseJoinPricingAndDormAndSlot> result = roomService.getBooking(
                        mockDorm.getId(), 1, 2, "R", pageable);
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
                verify(roomRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            }
        }

        @Test
        @DisplayName("getBooking: Should throw SURVEY_NOT_FOUND")
        void getBooking_SurveyNotFound() {
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(false);
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.getBooking(null, null, null, null, pageable))
                    .withMessage("SURVEY_NOT_FOUND");
        }

        @AfterEach
        void tearDown() {
            mockedSecurityUtils.close();
        }
    }

    @Nested
    @DisplayName("current Tests")
    class CurrentTests {
        private MockedStatic<SecurityUtils> mockedSecurityUtils;

        @BeforeEach
        void setup() {
            mockedSecurityUtils = mockStatic(SecurityUtils.class);
            mockedSecurityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        }

        @Test
        @DisplayName("current: Should return RoomResponseJoinDorm for current user's room")
        void current_Success() {
            Slot mockSlot = new Slot();
            mockSlot.setRoom(mockRoom);
            RoomResponseJoinDorm expectedResponse = new RoomResponseJoinDorm();
            expectedResponse.setId(roomId);
            when(slotRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockSlot));
            when(modelMapper.map(mockRoom, RoomResponseJoinDorm.class)).thenReturn(expectedResponse);
            RoomResponseJoinDorm actualResponse = roomService.current();
            assertThat(actualResponse).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("current: Should throw ROOM_NOT_FOUND if user has no slot")
        void current_NotFound() {
            when(slotRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.current())
                    .withMessage("ROOM_NOT_FOUND");
        }

        @AfterEach
        void tearDown() {
            mockedSecurityUtils.close();
        }
    }

    @Nested
    @DisplayName("update(Room) Tests")
    class UpdateRoomTests {
        @BeforeEach
        void setup() {
            Room currentRoomState = new Room();
            currentRoomState.setId(roomId);
            currentRoomState.setTotalSlot(mockRoom.getTotalSlot());
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(currentRoomState));
            when(roomRepository.findUsers(any(Room.class))).thenReturn(List.of());
            when(roomPricingService.getOrCreate(anyInt())).thenReturn(mockRoomPricing);
            when(roomRepository.exists(any(Specification.class))).thenReturn(false);
            when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));
            when(slotService.getByRoom(any(Room.class))).thenReturn(List.of(new Slot()));
        }

        @Test
        @DisplayName("update(Room): Should update room without changing slots if totalSlot is same")
        void update_Room_SuccessNoSlotChange() {
            mockRoom.setTotalSlot(2);
            mockRoom.setFloor(1);
            Room result = roomService.update(mockRoom);
            assertThat(result.getPricing()).isEqualTo(mockRoomPricing);
            verify(roomRepository, times(1)).save(mockRoom);
            verify(slotService, never()).deleteByRoom(any());
            verify(slotService, never()).create(any());
            verify(slotService, times(1)).getByRoom(mockRoom);
        }

        @Test
        @DisplayName("update(Room): Should delete old slots and create new ones if totalSlot changes")
        void update_Room_SlotChange() {
            mockRoom.setTotalSlot(4);
            mockRoom.setFloor(1);
            when(slotService.create(any(Room.class))).thenReturn(List.of(new Slot(), new Slot()));
            Room result = roomService.update(mockRoom);
            assertThat(result.getTotalSlot()).isEqualTo(4);
            verify(roomRepository, times(1)).save(mockRoom);
            verify(slotService, times(1)).deleteByRoom(mockRoom);
            verify(slotService, times(1)).create(mockRoom);
            verify(slotService, never()).getByRoom(any());
        }

        @Test
        @DisplayName("update(Room): Should throw INVALID_FLOOR if floor is too high")
        void update_Room_InvalidFloorHigh() {
            mockRoom.setFloor(6);
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.update(mockRoom))
                    .withMessage("INVALID_FLOOR");
            verify(roomRepository, never()).save(any());
        }

        @Test
        @DisplayName("update(Room): Should throw ROOM_NUMBER_EXISTED for different room with same number in dorm")
        void update_Room_RoomNumberExisted() {
            when(roomRepository.exists(any(Specification.class))).thenReturn(true);
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.update(mockRoom))
                    .withMessage("ROOM_NUMBER_EXISTED");
            verify(roomRepository, never()).save(any());
        }

        @Test
        @DisplayName("update(Room): Should throw ALREADY_HAVE_USERS if attempting to modify slot count")
        void update_Room_AlreadyHaveUsers() {
            mockRoom.setTotalSlot(4);
            when(roomRepository.findUsers(any(Room.class))).thenReturn(List.of(mockUser));
            assertThatExceptionOfType(AppException.class)
                    .isThrownBy(() -> roomService.update(mockRoom))
                    .withMessage("ALREADY_HAVE_USERS");
            verify(roomRepository, never()).save(any());
        }

        @Test
        @DisplayName("update(Room): Should not throw ALREADY_HAVE_USERS if totalSlot is unchanged and users are present")
        void update_Room_UsersPresentButNoSlotChange() {
            mockRoom.setTotalSlot(2);
            mockRoom.setFloor(1);
            when(roomRepository.findUsers(any(Room.class))).thenReturn(List.of(mockUser));
            Room result = roomService.update(mockRoom);
            assertThat(result).isNotNull();
            verify(roomRepository, times(1)).save(mockRoom);
            verify(slotService, never()).deleteByRoom(any());
        }
    }
}