package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.DormStatus;
import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.dto.request.dorm.UpdateDormRequest;
import com.capstone.capstone.dto.request.room.CreateRoomRequest;
import com.capstone.capstone.dto.response.dorm.DormResponse;
import com.capstone.capstone.dto.response.dorm.DormResponseJoinRoomSlot;
import com.capstone.capstone.dto.response.room.RoomResponseJoinPricing;
import com.capstone.capstone.dto.response.room.RoomResponseJoinPricingAndDorm;
import com.capstone.capstone.entity.Dorm;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.DormRepository;
import com.capstone.capstone.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DormServiceTest {
    @Mock
    private DormRepository dormRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private RoomService roomService;
    @InjectMocks
    private DormService dormService;
    private Dorm mockDorm;
    private Room mockRoom;
    private CreateDormRequest createDormRequest;
    private UpdateDormRequest updateDormRequest;
    private CreateRoomRequest createRoomRequest;
    private DormResponse dormResponse;

    @BeforeEach
    void setUp() {
        mockDorm = new Dorm();
        mockDorm.setId(UUID.randomUUID());
        mockDorm.setDormName("Dorm A");
        mockDorm.setTotalFloor(10);
        mockDorm.setTotalRoom(0);
        mockDorm.setStatus(DormStatus.ACTIVE);
        mockRoom = new Room();
        mockRoom.setId(UUID.randomUUID());
        mockRoom.setRoomNumber("Room 101");
        mockRoom.setFloor(1);
        mockRoom.setDorm(mockDorm);
        createDormRequest = new CreateDormRequest();
        createDormRequest.setDormName("Dorm A");
        createDormRequest.setTotalFloor(10);
        updateDormRequest = new UpdateDormRequest();
        updateDormRequest.setDormName("Updated Dorm A");
        updateDormRequest.setTotalFloor(12);
        createRoomRequest = new CreateRoomRequest();
        createRoomRequest.setRoomNumber("Room 101");
        createRoomRequest.setFloor(1);
        dormResponse = new DormResponse();
        dormResponse.setId(mockDorm.getId());
        dormResponse.setDormName(mockDorm.getDormName());
        dormResponse.setTotalFloor(mockDorm.getTotalFloor());
    }

    @Test
    void create_WithRequest_Success() {
        when(modelMapper.map(createDormRequest, Dorm.class)).thenReturn(mockDorm);
        when(dormRepository.exists(ArgumentMatchers.<Specification<Dorm>>any())).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        when(modelMapper.map(mockDorm, DormResponse.class)).thenReturn(dormResponse);
        DormResponse result = dormService.create(createDormRequest);
        assertNotNull(result);
        assertEquals(dormResponse.getId(), result.getId());
        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(0, dormCaptor.getValue().getTotalRoom());
    }

    @Test
    void create_WithNameAndFloor_Success() {
        String dormName = "Dorm B";
        Integer totalFloor = 8;
        when(dormRepository.exists(ArgumentMatchers.<Specification<Dorm>>any())).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        Dorm result = dormService.create(dormName, totalFloor);
        assertNotNull(result);
        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(dormName, dormCaptor.getValue().getDormName());
        assertEquals(totalFloor, dormCaptor.getValue().getTotalFloor());
    }

    @Test
    void create_WithDorm_Success_SetsDefaultStatus() {
        mockDorm.setStatus(null);
        when(dormRepository.exists(ArgumentMatchers.<Specification<Dorm>>any())).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        Dorm result = dormService.create(mockDorm);
        assertNotNull(result);
        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(DormStatus.ACTIVE, dormCaptor.getValue().getStatus());
    }

    @Test
    void create_WithDorm_Success_KeepsExistingStatus() {
        mockDorm.setStatus(DormStatus.INACTIVE);
        when(dormRepository.exists(ArgumentMatchers.<Specification<Dorm>>any())).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        Dorm result = dormService.create(mockDorm);
        assertNotNull(result);
        verify(dormRepository).save(mockDorm);
        assertEquals(DormStatus.INACTIVE, mockDorm.getStatus());
    }

    @Test
    void create_WithDorm_ThrowsException_DormNameExists() {
        when(dormRepository.exists(ArgumentMatchers.<Specification<Dorm>>any())).thenReturn(true);
        AppException exception = assertThrows(AppException.class, () -> dormService.create(mockDorm));
        assertEquals("DORM_NAME_EXISTED", exception.getMessage());
        verify(dormRepository, never()).save(any(Dorm.class));
    }

    @Test
    void getAllResponse_Success_ReturnsAllDorms() {
        Dorm dorm2 = new Dorm();
        dorm2.setId(UUID.randomUUID());
        dorm2.setDormName("Dorm B");
        List<Dorm> dorms = Arrays.asList(mockDorm, dorm2);
        DormResponse dormResponse2 = new DormResponse();
        dormResponse2.setId(dorm2.getId());
        dormResponse2.setDormName(dorm2.getDormName());
        when(dormRepository.findAll()).thenReturn(dorms);
        when(modelMapper.map(mockDorm, DormResponse.class)).thenReturn(dormResponse);
        when(modelMapper.map(dorm2, DormResponse.class)).thenReturn(dormResponse2);
        List<DormResponse> result = dormService.getAllResponse();
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(dormRepository).findAll();
    }

    @Test
    void getAllResponse_EmptyList_ReturnsEmptyList() {
        when(dormRepository.findAll()).thenReturn(List.of());
        List<DormResponse> result = dormService.getAllResponse();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_Success_ReturnsAllDorms() {
        List<Dorm> dorms = Arrays.asList(mockDorm, new Dorm());
        when(dormRepository.findAll()).thenReturn(dorms);
        List<Dorm> result = dormService.getAll();
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(dormRepository).findAll();
    }

    @Test
    void getResponseById_Success_ReturnsDormWithRoomSlot() {
        UUID dormId = mockDorm.getId();
        DormResponseJoinRoomSlot response = new DormResponseJoinRoomSlot();
        response.setId(dormId);
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        when(modelMapper.map(Optional.of(mockDorm), DormResponseJoinRoomSlot.class)).thenReturn(response);
        DormResponseJoinRoomSlot result = dormService.getResponseById(dormId);
        assertNotNull(result);
        assertEquals(dormId, result.getId());
    }

    @Test
    void getById_Success_ReturnsDorm() {
        UUID dormId = mockDorm.getId();
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        Optional<Dorm> result = dormService.getById(dormId);
        assertTrue(result.isPresent());
        assertEquals(mockDorm, result.get());
    }

    @Test
    void getById_NotFound_ReturnsEmpty() {
        UUID dormId = UUID.randomUUID();
        when(dormRepository.findById(dormId)).thenReturn(Optional.empty());
        Optional<Dorm> result = dormService.getById(dormId);
        assertFalse(result.isPresent());
    }

    @Test
    void getRooms_WithDormId_Success_ReturnsRoomList() {
        UUID dormId = mockDorm.getId();
        List<Room> rooms = Collections.singletonList(mockRoom);
        RoomResponseJoinPricing roomResponse = new RoomResponseJoinPricing();
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        when(roomService.getAllByDorm(mockDorm)).thenReturn(rooms);
        when(modelMapper.map(mockRoom, RoomResponseJoinPricing.class)).thenReturn(roomResponse);
        List<RoomResponseJoinPricing> result = dormService.getRooms(dormId);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roomService).getAllByDorm(mockDorm);
    }

    @Test
    void getRooms_WithDormId_ThrowsException_DormNotFound() {
        UUID dormId = UUID.randomUUID();
        when(dormRepository.findById(dormId)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class,
                () -> dormService.getRooms(dormId));
        assertEquals("DORM_NOT_FOUND", exception.getMessage());
    }

    @Test
    void getRooms_WithDorm_Success_ReturnsRoomList() {
        List<Room> rooms = Collections.singletonList(mockRoom);
        when(roomService.getAllByDorm(mockDorm)).thenReturn(rooms);
        List<Room> result = dormService.getRooms(mockDorm);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roomService).getAllByDorm(mockDorm);
    }

    @Test
    void addRoom_WithDormIdAndRequest_Success() {
        UUID dormId = mockDorm.getId();
        RoomResponseJoinPricingAndDorm roomResponse = new RoomResponseJoinPricingAndDorm();
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        when(modelMapper.map(createRoomRequest, Room.class)).thenReturn(mockRoom);
        when(roomService.create(any(Room.class))).thenReturn(mockRoom);
        when(roomRepository.count(ArgumentMatchers.<Specification<Room>>any())).thenReturn(1L);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        when(modelMapper.map(mockRoom, RoomResponseJoinPricingAndDorm.class)).thenReturn(roomResponse);
        RoomResponseJoinPricingAndDorm result = dormService.addRoom(dormId, createRoomRequest);
        assertNotNull(result);
        verify(roomService).create(any(Room.class));
        verify(dormRepository).save(any(Dorm.class));
    }

    @Test
    void addRoom_WithDormIdAndRequest_ThrowsException_DormNotFound() {
        UUID dormId = UUID.randomUUID();
        when(dormRepository.findById(dormId)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class,
                () -> dormService.addRoom(dormId, createRoomRequest));
        assertEquals("DORM_NOT_FOUND", exception.getMessage());
    }

    @Test
    void addRoom_WithDormAndRoom_Success() {
        mockRoom.setFloor(5);
        when(roomService.create(any(Room.class))).thenReturn(mockRoom);
        when(roomRepository.count(ArgumentMatchers.<Specification<Room>>any())).thenReturn(1L);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        Room result = dormService.addRoom(mockDorm, mockRoom);
        assertNotNull(result);
        assertEquals(mockDorm, result.getDorm());
        verify(roomService).create(mockRoom);
        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(1, dormCaptor.getValue().getTotalRoom());
    }

    @Test
    void addRoom_WithDormAndRoom_ThrowsException_InvalidFloorZero() {
        mockRoom.setFloor(0);
        AppException exception = assertThrows(AppException.class, () -> dormService.addRoom(mockDorm, mockRoom));
        assertEquals("INVALID_FLOOR", exception.getMessage());
        verify(roomService, never()).create(any(Room.class));
    }

    @Test
    void addRoom_WithDormAndRoom_ThrowsException_InvalidFloorNegative() {
        mockRoom.setFloor(-1);
        AppException exception = assertThrows(AppException.class, () -> dormService.addRoom(mockDorm, mockRoom));
        assertEquals("INVALID_FLOOR", exception.getMessage());
    }

    @Test
    void addRoom_WithDormAndRoom_ThrowsException_InvalidFloorTooHigh() {
        mockRoom.setFloor(11);
        AppException exception = assertThrows(AppException.class, () -> dormService.addRoom(mockDorm, mockRoom));
        assertEquals("INVALID_FLOOR", exception.getMessage());
    }

    @Test
    void addRoom_WithDormAndRoom_Success_FloorEqualsMaxFloor() {
        mockRoom.setFloor(10);
        when(roomService.create(any(Room.class))).thenReturn(mockRoom);
        when(roomRepository.count(ArgumentMatchers.<Specification<Room>>any())).thenReturn(1L);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        Room result = dormService.addRoom(mockDorm, mockRoom);
        assertNotNull(result);
        verify(roomService).create(mockRoom);
    }

    @Test
    void update_WithIdAndRequest_Success() {
        UUID dormId = mockDorm.getId();
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        when(dormRepository.exists(ArgumentMatchers.<Specification<Dorm>>any())).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        DormResponse updatedResponse = new DormResponse();
        updatedResponse.setId(dormId);
        updatedResponse.setDormName(updateDormRequest.getDormName());
        when(modelMapper.map(any(Dorm.class), eq(DormResponse.class))).thenReturn(updatedResponse);
        DormResponse result = dormService.update(dormId, updateDormRequest);
        assertNotNull(result);
        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(dormId, dormCaptor.getValue().getId());
        assertEquals(updateDormRequest.getDormName(), dormCaptor.getValue().getDormName());
        assertEquals(updateDormRequest.getTotalFloor(), dormCaptor.getValue().getTotalFloor());
    }

    @Test
    void update_WithIdAndRequest_ThrowsException_DormNotFound() {
        UUID dormId = UUID.randomUUID();
        when(dormRepository.findById(dormId)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class,
                () -> dormService.update(dormId, updateDormRequest));
        assertEquals("DORM_NOT_FOUND", exception.getMessage());
    }

    @Test
    void update_WithDorm_Success() {
        when(dormRepository.exists(ArgumentMatchers.<Specification<Dorm>>any())).thenReturn(false);
        when(dormRepository.save(mockDorm)).thenReturn(mockDorm);
        Dorm result = dormService.update(mockDorm);
        assertNotNull(result);
        assertEquals(mockDorm, result);
        verify(dormRepository).save(mockDorm);
    }

    @Test
    void update_WithDorm_ThrowsException_DormNameExistsForOtherDorm() {
        when(dormRepository.exists(ArgumentMatchers.<Specification<Dorm>>any())).thenReturn(true);
        AppException exception = assertThrows(AppException.class, () -> dormService.update(mockDorm));
        assertEquals("DORM_NAME_EXISTED", exception.getMessage());
        verify(dormRepository, never()).save(any(Dorm.class));
    }

    @Test
    void updateTotalRoom_Success_UpdatesRoomCount() {
        when(roomRepository.count(ArgumentMatchers.<Specification<Room>>any())).thenReturn(5L);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        Dorm result = dormService.updateTotalRoom(mockDorm);
        assertNotNull(result);
        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(5, dormCaptor.getValue().getTotalRoom());
    }

    @Test
    void updateTotalRoom_Success_WithZeroRooms() {
        when(roomRepository.count(ArgumentMatchers.<Specification<Room>>any())).thenReturn(0L);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        Dorm result = dormService.updateTotalRoom(mockDorm);
        assertNotNull(result);
        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(0, dormCaptor.getValue().getTotalRoom());
    }

    @Test
    void create_AndAddRoom_Integration_Success() {
        when(dormRepository.exists(ArgumentMatchers.<Specification<Dorm>>any())).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomService.create(any(Room.class))).thenReturn(mockRoom);
        when(roomRepository.count(ArgumentMatchers.<Specification<Room>>any())).thenReturn(1L);
        Dorm createdDorm = dormService.create(mockDorm);
        Room addedRoom = dormService.addRoom(createdDorm, mockRoom);
        assertNotNull(createdDorm);
        assertNotNull(addedRoom);
        assertEquals(createdDorm, addedRoom.getDorm());
        verify(dormRepository, atLeast(2)).save(any(Dorm.class));
    }

    @Test
    void update_WithSameName_Success() {
        UUID dormId = mockDorm.getId();
        updateDormRequest.setDormName(mockDorm.getDormName());
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        when(dormRepository.exists(ArgumentMatchers.<Specification<Dorm>>any())).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        when(modelMapper.map(any(Dorm.class), eq(DormResponse.class))).thenReturn(dormResponse);
        DormResponse result = dormService.update(dormId, updateDormRequest);
        assertNotNull(result);
        verify(dormRepository).save(any(Dorm.class));
    }

    @Test
    void addRoom_MultipleRooms_UpdatesTotalRoomCorrectly() {
        Room room2 = new Room();
        room2.setId(UUID.randomUUID());
        room2.setFloor(2);
        when(roomService.create(any(Room.class))).thenReturn(mockRoom)
                .thenReturn(room2);
        when(roomRepository.count(ArgumentMatchers.<Specification<Room>>any()))
                .thenReturn(1L).thenReturn(2L);
        when(dormRepository.save(any(Dorm.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Room result1 = dormService.addRoom(mockDorm, mockRoom);
        Room result2 = dormService.addRoom(mockDorm, room2);
        assertNotNull(result1);
        assertNotNull(result2);
        verify(dormRepository, times(2)).save(any(Dorm.class));
    }
}