package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.request.room.UpdateRoomRequest;
import com.capstone.capstone.dto.response.booking.UserMatching;
import com.capstone.capstone.dto.response.room.*;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.MatchingRepository;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.repository.SurveyQuestionRepository;
import com.capstone.capstone.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    @Mock
    private RoomRepository roomRepository;
    @Spy
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
    private BookingValidateService bookingValidateService;
    @InjectMocks
    private RoomService roomService;
    private MockedStatic<SecurityUtils> securityUtilsMock;
    private User mockUser;
    private Room mockRoom;
    private Dorm mockDorm;
    private RoomPricing mockPricing;

    @BeforeEach
    void setUp() {
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setGender(GenderEnum.MALE);
        mockUser.setUsername("testUser");
        mockDorm = new Dorm();
        mockDorm.setId(UUID.randomUUID());
        mockDorm.setDormName("Dorm A");
        mockDorm.setTotalFloor(5);
        mockPricing = new RoomPricing();
        mockPricing.setId(UUID.randomUUID());
        mockPricing.setTotalSlot(4);
        mockPricing.setPrice(1000L);
        mockRoom = new Room();
        mockRoom.setId(UUID.randomUUID());
        mockRoom.setRoomNumber("101");
        mockRoom.setDorm(mockDorm);
        mockRoom.setTotalSlot(4);
        mockRoom.setFloor(1);
        mockRoom.setStatus(StatusRoomEnum.AVAILABLE);
        mockRoom.setPricing(mockPricing);
        mockRoom.setSlots(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void getMatching_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        doNothing().when(bookingValidateService).validate();
        when(surveyQuestionRepository.count()).thenReturn(10L);
        when(roomRepository.findAvailableForGender(mockUser.getGender())).thenReturn(new ArrayList<>(List.of(mockRoom)));
        RoomMatching matchingMock = mock(RoomMatching.class);
        when(matchingMock.getRoomId()).thenReturn(mockRoom.getId());
        when(matchingMock.getUserCount()).thenReturn(2);
        when(matchingMock.getSameOptionCount()).thenReturn(15);
        when(matchingRepository.computeRoomMatching(eq(mockUser), anyList())).thenReturn(List.of(matchingMock));
        List<RoomMatchingResponse> result = roomService.getMatching();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(75.0, result.getFirst().getMatching());
    }

    @Test
    void getResponseById_Success() {
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(modelMapper.map(mockRoom, RoomResponseJoinPricingAndDormAndSlot.class)).thenReturn(new RoomResponseJoinPricingAndDormAndSlot());
        var result = roomService.getResponseById(mockRoom.getId());
        assertNotNull(result);
    }

    @Test
    void getResponseById_NotFound() {
        UUID id = UUID.randomUUID();
        when(roomRepository.findById(id)).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> roomService.getResponseById(id));
        assertEquals("ROOM_NOT_FOUND", ex.getMessage());
    }

    @Test
    void getRoommates_Success() {
        Slot userSlot = new Slot();
        userSlot.setRoom(mockRoom);
        mockUser.setSlot(userSlot);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        when(surveyQuestionRepository.count()).thenReturn(10L);
        User roommate = new User();
        roommate.setId(UUID.randomUUID());
        when(roomRepository.findUsers(mockRoom)).thenReturn(List.of(roommate));
        UserMatching matchingMock = mock(UserMatching.class);
        when(matchingMock.getId()).thenReturn(roommate.getId());
        when(matchingMock.getSameOptionCount()).thenReturn(8.0);
        when(matchingRepository.computeUserMatching(eq(mockUser), anyList())).thenReturn(List.of(matchingMock));
        RoommateResponse responseDto = new RoommateResponse();
        responseDto.setId(roommate.getId());
        when(modelMapper.map(roommate, RoommateResponse.class)).thenReturn(responseDto);
        List<RoommateResponse> result = roomService.getRoommates();
        assertEquals(1, result.size());
        assertEquals(80.0, result.get(0).getMatching());
    }

    @Test
    void getRoommates_NoSlot() {
        mockUser.setSlot(null);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        AppException ex = assertThrows(AppException.class, () -> roomService.getRoommates());
        assertEquals("SLOT_NOT_FOUND", ex.getMessage());
    }

    @Test
    void checkFullAndUpdate_IsFull() {
        when(roomRepository.isFull(mockRoom)).thenReturn(true);
        roomService.checkFullAndUpdate(mockRoom);
        assertEquals(StatusRoomEnum.FULL, mockRoom.getStatus());
        verify(roomRepository).save(mockRoom);
    }

    @Test
    void checkFullAndUpdate_Available() {
        when(roomRepository.isFull(mockRoom)).thenReturn(false);
        roomService.checkFullAndUpdate(mockRoom);
        assertEquals(StatusRoomEnum.AVAILABLE, mockRoom.getStatus());
        verify(roomRepository).save(mockRoom);
    }

    @Test
    void get_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(roomRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(mockRoom)));
        var result = roomService.get(new HashMap<>(), pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getBooking_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        doNothing().when(bookingValidateService).validate();
        when(roomRepository.findAvailableForGender(mockUser.getGender())).thenReturn(List.of(mockRoom));
        Pageable pageable = PageRequest.of(0, 10);
        when(roomRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(mockRoom)));
        var result = roomService.getBooking(mockDorm.getId(), 1, 4, "101", pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void current_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        Slot slot = new Slot();
        slot.setRoom(mockRoom);
        when(slotRepository.findOne(any(Specification.class))).thenReturn(Optional.of(slot));
        when(modelMapper.map(mockRoom, RoomResponseJoinDorm.class)).thenReturn(new RoomResponseJoinDorm());
        assertNotNull(roomService.current());
    }

    @Test
    void current_NotFound() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        when(slotRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> roomService.current());
        assertEquals("ROOM_NOT_FOUND", ex.getMessage());
    }

    @Test
    void create_Single_Success() {
        mockRoom.setPricing(null);
        when(roomRepository.exists(any(Specification.class))).thenReturn(false);
        when(roomPricingService.getOrCreate(4)).thenReturn(mockPricing);
        when(roomRepository.save(mockRoom)).thenReturn(mockRoom);
        when(slotService.create(mockRoom)).thenReturn(new ArrayList<>());
        Room result = roomService.create(mockRoom);
        assertNotNull(result);
        assertEquals(mockPricing, result.getPricing());
        verify(slotService).create(mockRoom);
    }

    @Test
    void create_Single_Fail_RoomNumberExisted() {
        when(roomRepository.exists(any(Specification.class))).thenReturn(true);
        AppException ex = assertThrows(AppException.class, () -> roomService.create(mockRoom));
        assertEquals("ROOM_NUMBER_EXISTED", ex.getMessage());
    }

    @Test
    void create_List_Success() {
        when(roomPricingService.getByTotalSlot(4)).thenReturn(Optional.of(mockPricing));
        when(roomRepository.saveAll(anyList())).thenReturn(List.of(mockRoom));
        List<Room> result = roomService.create(List.of(mockRoom));
        assertEquals(1, result.size());
        verify(slotService).create(mockRoom);
    }

    @Test
    void create_List_Fail_PricingNotFound() {
        when(roomPricingService.getByTotalSlot(4)).thenReturn(Optional.empty());
        List<Room> list = List.of(mockRoom);
        AppException ex = assertThrows(AppException.class, () -> roomService.create(list));
        assertEquals("ROOM_TYPE_NOT_EXIST", ex.getMessage());
    }

    @Test
    void update_Success_NoSlotChange() {
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setRoomNumber("102");
        request.setTotalSlot(4);
        request.setFloor(1);
        request.setStatus(StatusRoomEnum.AVAILABLE);
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(roomRepository.exists(any(Specification.class))).thenReturn(false);
        when(roomRepository.findUsers(any(Room.class))).thenReturn(new ArrayList<>());
        when(roomPricingService.getOrCreate(4)).thenReturn(mockPricing);
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);
        when(slotService.getByRoom(any(Room.class))).thenReturn(new ArrayList<>());
        var result = roomService.update(mockRoom.getId(), request);
        assertNotNull(result);
        verify(slotService, never()).deleteByRoom(any());
    }

    @Test
    void update_Success_SlotChange() {
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setRoomNumber("101");
        request.setTotalSlot(2);
        request.setFloor(1);
        request.setStatus(StatusRoomEnum.AVAILABLE);
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(roomRepository.exists(any(Specification.class))).thenReturn(false);
        when(roomRepository.findUsers(any(Room.class))).thenReturn(new ArrayList<>());
        when(roomPricingService.getOrCreate(2)).thenReturn(mockPricing);
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);
        when(slotService.create(any(Room.class))).thenReturn(new ArrayList<>());
        roomService.update(mockRoom.getId(), request);
        verify(slotService).deleteByRoom(any(Room.class));
        verify(slotService).create(any(Room.class));
    }

    @Test
    void update_Fail_RoomNumberExisted() {
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setRoomNumber("102");
        request.setTotalSlot(4);
        request.setFloor(1);
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(roomRepository.exists(any(Specification.class))).thenReturn(true);
        AppException ex = assertThrows(AppException.class, () -> roomService.update(mockRoom.getId(), request));
        assertEquals("ROOM_NUMBER_EXISTED", ex.getMessage());
    }

    @Test
    void update_Fail_HasUsers() {
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setRoomNumber("101");
        request.setTotalSlot(4);
        request.setFloor(1);
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(roomRepository.exists(any(Specification.class))).thenReturn(false);
        when(roomRepository.findUsers(any(Room.class))).thenReturn(List.of(mockUser));
        AppException ex = assertThrows(AppException.class, () -> roomService.update(mockRoom.getId(), request));
        assertEquals("ALREADY_HAVE_USERS", ex.getMessage());
    }

    @Test
    void update_Fail_InvalidFloor() {
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setFloor(100);
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        AppException ex = assertThrows(AppException.class, () -> roomService.update(mockRoom.getId(), request));
        assertEquals("INVALID_FLOOR", ex.getMessage());
    }

    @Test
    void getUsersResponse_Success() {
        Slot slot = new Slot();
        slot.setUser(mockUser);
        mockRoom.setSlots(List.of(slot));
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(modelMapper.map(mockUser, RoomUserResponse.class)).thenReturn(new RoomUserResponse());
        List<RoomUserResponse> result = roomService.getUsersResponse(mockRoom.getId());
        assertEquals(1, result.size());
    }

    @Test
    void getAllByDorm_Success() {
        when(roomRepository.findAll(any(Specification.class))).thenReturn(List.of(mockRoom));
        List<Room> result = roomService.getAllByDorm(mockDorm);
        assertEquals(1, result.size());
    }
}