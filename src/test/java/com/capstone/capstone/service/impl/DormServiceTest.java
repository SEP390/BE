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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    // ========== create(CreateDormRequest) Tests ==========

    @Test
    void create_WithRequest_Success() {
        // Arrange
        when(modelMapper.map(createDormRequest, Dorm.class)).thenReturn(mockDorm);
        when(dormRepository.exists(any(Specification.class))).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        when(modelMapper.map(mockDorm, DormResponse.class)).thenReturn(dormResponse);

        // Act
        DormResponse result = dormService.create(createDormRequest);

        // Assert
        assertNotNull(result);
        assertEquals(dormResponse.getId(), result.getId());

        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(0, dormCaptor.getValue().getTotalRoom());
    }

    // ========== create(String, Integer) Tests ==========

    @Test
    void create_WithNameAndFloor_Success() {
        // Arrange
        String dormName = "Dorm B";
        Integer totalFloor = 8;

        when(dormRepository.exists(any(Specification.class))).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);

        // Act
        Dorm result = dormService.create(dormName, totalFloor);

        // Assert
        assertNotNull(result);

        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(dormName, dormCaptor.getValue().getDormName());
        assertEquals(totalFloor, dormCaptor.getValue().getTotalFloor());
    }

    // ========== create(Dorm) Tests ==========

    @Test
    void create_WithDorm_Success_SetsDefaultStatus() {
        // Arrange
        mockDorm.setStatus(null);
        when(dormRepository.exists(any(Specification.class))).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);

        // Act
        Dorm result = dormService.create(mockDorm);

        // Assert
        assertNotNull(result);

        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(DormStatus.ACTIVE, dormCaptor.getValue().getStatus());
    }

    @Test
    void create_WithDorm_Success_KeepsExistingStatus() {
        // Arrange
        mockDorm.setStatus(DormStatus.INACTIVE);
        when(dormRepository.exists(any(Specification.class))).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);

        // Act
        Dorm result = dormService.create(mockDorm);

        // Assert
        assertNotNull(result);
        verify(dormRepository).save(mockDorm);
        assertEquals(DormStatus.INACTIVE, mockDorm.getStatus());
    }

    @Test
    void create_WithDorm_ThrowsException_DormNameExists() {
        // Arrange
        when(dormRepository.exists(any(Specification.class))).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> dormService.create(mockDorm));
        assertEquals("DORM_NAME_EXISTED", exception.getMessage());
        verify(dormRepository, never()).save(any(Dorm.class));
    }

    // ========== getAllResponse() Tests ==========

    @Test
    void getAllResponse_Success_ReturnsAllDorms() {
        // Arrange
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

        // Act
        List<DormResponse> result = dormService.getAllResponse();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(dormRepository).findAll();
    }

    @Test
    void getAllResponse_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(dormRepository.findAll()).thenReturn(List.of());

        // Act
        List<DormResponse> result = dormService.getAllResponse();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getAll() Tests ==========

    @Test
    void getAll_Success_ReturnsAllDorms() {
        // Arrange
        List<Dorm> dorms = Arrays.asList(mockDorm, new Dorm());
        when(dormRepository.findAll()).thenReturn(dorms);

        // Act
        List<Dorm> result = dormService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(dormRepository).findAll();
    }

    // ========== getResponseById() Tests ==========

    @Test
    void getResponseById_Success_ReturnsDormWithRoomSlot() {
        // Arrange
        UUID dormId = mockDorm.getId();
        DormResponseJoinRoomSlot response = new DormResponseJoinRoomSlot();
        response.setId(dormId);

        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        when(modelMapper.map(Optional.of(mockDorm), DormResponseJoinRoomSlot.class)).thenReturn(response);

        // Act
        DormResponseJoinRoomSlot result = dormService.getResponseById(dormId);

        // Assert
        assertNotNull(result);
        assertEquals(dormId, result.getId());
    }

    // ========== getById() Tests ==========

    @Test
    void getById_Success_ReturnsDorm() {
        // Arrange
        UUID dormId = mockDorm.getId();
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));

        // Act
        Optional<Dorm> result = dormService.getById(dormId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockDorm, result.get());
    }

    @Test
    void getById_NotFound_ReturnsEmpty() {
        // Arrange
        UUID dormId = UUID.randomUUID();
        when(dormRepository.findById(dormId)).thenReturn(Optional.empty());

        // Act
        Optional<Dorm> result = dormService.getById(dormId);

        // Assert
        assertFalse(result.isPresent());
    }

    // ========== getRooms(UUID) Tests ==========

    @Test
    void getRooms_WithDormId_Success_ReturnsRoomList() {
        // Arrange
        UUID dormId = mockDorm.getId();
        List<Room> rooms = Arrays.asList(mockRoom);
        RoomResponseJoinPricing roomResponse = new RoomResponseJoinPricing();

        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        when(roomService.getAllByDorm(mockDorm)).thenReturn(rooms);
        when(modelMapper.map(mockRoom, RoomResponseJoinPricing.class)).thenReturn(roomResponse);

        // Act
        List<RoomResponseJoinPricing> result = dormService.getRooms(dormId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roomService).getAllByDorm(mockDorm);
    }

    @Test
    void getRooms_WithDormId_ThrowsException_DormNotFound() {
        // Arrange
        UUID dormId = UUID.randomUUID();
        when(dormRepository.findById(dormId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> dormService.getRooms(dormId));
        assertEquals("DORM_NOT_FOUND", exception.getMessage());
    }

    // ========== getRooms(Dorm) Tests ==========

    @Test
    void getRooms_WithDorm_Success_ReturnsRoomList() {
        // Arrange
        List<Room> rooms = Arrays.asList(mockRoom);
        when(roomService.getAllByDorm(mockDorm)).thenReturn(rooms);

        // Act
        List<Room> result = dormService.getRooms(mockDorm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roomService).getAllByDorm(mockDorm);
    }

    // ========== addRoom(UUID, CreateRoomRequest) Tests ==========

    @Test
    void addRoom_WithDormIdAndRequest_Success() {
        // Arrange
        UUID dormId = mockDorm.getId();
        RoomResponseJoinPricingAndDorm roomResponse = new RoomResponseJoinPricingAndDorm();

        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        when(modelMapper.map(createRoomRequest, Room.class)).thenReturn(mockRoom);
        when(roomService.create(any(Room.class))).thenReturn(mockRoom);
        when(roomRepository.count(any(Specification.class))).thenReturn(1L);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        when(modelMapper.map(mockRoom, RoomResponseJoinPricingAndDorm.class)).thenReturn(roomResponse);

        // Act
        RoomResponseJoinPricingAndDorm result = dormService.addRoom(dormId, createRoomRequest);

        // Assert
        assertNotNull(result);
        verify(roomService).create(any(Room.class));
        verify(dormRepository).save(any(Dorm.class));
    }

    @Test
    void addRoom_WithDormIdAndRequest_ThrowsException_DormNotFound() {
        // Arrange
        UUID dormId = UUID.randomUUID();
        when(dormRepository.findById(dormId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> dormService.addRoom(dormId, createRoomRequest));
        assertEquals("DORM_NOT_FOUND", exception.getMessage());
    }

    // ========== addRoom(Dorm, Room) Tests ==========

    @Test
    void addRoom_WithDormAndRoom_Success() {
        // Arrange
        mockRoom.setFloor(5);
        when(roomService.create(any(Room.class))).thenReturn(mockRoom);
        when(roomRepository.count(any(Specification.class))).thenReturn(1L);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);

        // Act
        Room result = dormService.addRoom(mockDorm, mockRoom);

        // Assert
        assertNotNull(result);
        assertEquals(mockDorm, result.getDorm());
        verify(roomService).create(mockRoom);

        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(1, dormCaptor.getValue().getTotalRoom());
    }

    @Test
    void addRoom_WithDormAndRoom_ThrowsException_InvalidFloorZero() {
        // Arrange
        mockRoom.setFloor(0);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> dormService.addRoom(mockDorm, mockRoom));
        assertEquals("INVALID_FLOOR", exception.getMessage());
        verify(roomService, never()).create(any(Room.class));
    }

    @Test
    void addRoom_WithDormAndRoom_ThrowsException_InvalidFloorNegative() {
        // Arrange
        mockRoom.setFloor(-1);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> dormService.addRoom(mockDorm, mockRoom));
        assertEquals("INVALID_FLOOR", exception.getMessage());
    }

    @Test
    void addRoom_WithDormAndRoom_ThrowsException_InvalidFloorTooHigh() {
        // Arrange
        mockRoom.setFloor(11); // mockDorm has totalFloor = 10

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> dormService.addRoom(mockDorm, mockRoom));
        assertEquals("INVALID_FLOOR", exception.getMessage());
    }

    @Test
    void addRoom_WithDormAndRoom_Success_FloorEqualsMaxFloor() {
        // Arrange
        mockRoom.setFloor(10); // Exactly the maximum floor
        when(roomService.create(any(Room.class))).thenReturn(mockRoom);
        when(roomRepository.count(any(Specification.class))).thenReturn(1L);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);

        // Act
        Room result = dormService.addRoom(mockDorm, mockRoom);

        // Assert
        assertNotNull(result);
        verify(roomService).create(mockRoom);
    }

    // ========== update(UUID, UpdateDormRequest) Tests ==========

    @Test
    void update_WithIdAndRequest_Success() {
        // Arrange
        UUID dormId = mockDorm.getId();
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        when(dormRepository.exists(any(Specification.class))).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);

        DormResponse updatedResponse = new DormResponse();
        updatedResponse.setId(dormId);
        updatedResponse.setDormName(updateDormRequest.getDormName());
        when(modelMapper.map(any(Dorm.class), eq(DormResponse.class))).thenReturn(updatedResponse);

        // Act
        DormResponse result = dormService.update(dormId, updateDormRequest);

        // Assert
        assertNotNull(result);

        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(dormId, dormCaptor.getValue().getId());
        assertEquals(updateDormRequest.getDormName(), dormCaptor.getValue().getDormName());
        assertEquals(updateDormRequest.getTotalFloor(), dormCaptor.getValue().getTotalFloor());
    }

    @Test
    void update_WithIdAndRequest_ThrowsException_DormNotFound() {
        // Arrange
        UUID dormId = UUID.randomUUID();
        when(dormRepository.findById(dormId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> dormService.update(dormId, updateDormRequest));
        assertEquals("DORM_NOT_FOUND", exception.getMessage());
    }

    // ========== update(Dorm) Tests ==========

    @Test
    void update_WithDorm_Success() {
        // Arrange
        when(dormRepository.exists(any(Specification.class))).thenReturn(false);
        when(dormRepository.save(mockDorm)).thenReturn(mockDorm);

        // Act
        Dorm result = dormService.update(mockDorm);

        // Assert
        assertNotNull(result);
        assertEquals(mockDorm, result);
        verify(dormRepository).save(mockDorm);
    }

    @Test
    void update_WithDorm_ThrowsException_DormNameExistsForOtherDorm() {
        // Arrange
        when(dormRepository.exists(any(Specification.class))).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> dormService.update(mockDorm));
        assertEquals("DORM_NAME_EXISTED", exception.getMessage());
        verify(dormRepository, never()).save(any(Dorm.class));
    }

    // ========== updateTotalRoom() Tests ==========

    @Test
    void updateTotalRoom_Success_UpdatesRoomCount() {
        // Arrange
        when(roomRepository.count(any(Specification.class))).thenReturn(5L);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);

        // Act
        Dorm result = dormService.updateTotalRoom(mockDorm);

        // Assert
        assertNotNull(result);

        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(5, dormCaptor.getValue().getTotalRoom());
    }

    @Test
    void updateTotalRoom_Success_WithZeroRooms() {
        // Arrange
        when(roomRepository.count(any(Specification.class))).thenReturn(0L);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);

        // Act
        Dorm result = dormService.updateTotalRoom(mockDorm);

        // Assert
        assertNotNull(result);

        ArgumentCaptor<Dorm> dormCaptor = ArgumentCaptor.forClass(Dorm.class);
        verify(dormRepository).save(dormCaptor.capture());
        assertEquals(0, dormCaptor.getValue().getTotalRoom());
    }

    // ========== Integration-like Tests ==========

    @Test
    void create_AndAddRoom_Integration_Success() {
        // Arrange
        when(dormRepository.exists(any(Specification.class))).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomService.create(any(Room.class))).thenReturn(mockRoom);
        when(roomRepository.count(any(Specification.class))).thenReturn(1L);

        // Act
        Dorm createdDorm = dormService.create(mockDorm);
        Room addedRoom = dormService.addRoom(createdDorm, mockRoom);

        // Assert
        assertNotNull(createdDorm);
        assertNotNull(addedRoom);
        assertEquals(createdDorm, addedRoom.getDorm());
        verify(dormRepository, atLeast(2)).save(any(Dorm.class));
    }

    @Test
    void update_WithSameName_Success() {
        // Arrange
        UUID dormId = mockDorm.getId();
        updateDormRequest.setDormName(mockDorm.getDormName()); // Same name

        when(dormRepository.findById(dormId)).thenReturn(Optional.of(mockDorm));
        when(dormRepository.exists(any(Specification.class))).thenReturn(false);
        when(dormRepository.save(any(Dorm.class))).thenReturn(mockDorm);
        when(modelMapper.map(any(Dorm.class), eq(DormResponse.class))).thenReturn(dormResponse);

        // Act
        DormResponse result = dormService.update(dormId, updateDormRequest);

        // Assert
        assertNotNull(result);
        verify(dormRepository).save(any(Dorm.class));
    }

    @Test
    void addRoom_MultipleRooms_UpdatesTotalRoomCorrectly() {
        // Arrange
        Room room2 = new Room();
        room2.setId(UUID.randomUUID());
        room2.setFloor(2);

        when(roomService.create(any(Room.class)))
                .thenReturn(mockRoom)
                .thenReturn(room2);
        when(roomRepository.count(any(Specification.class)))
                .thenReturn(1L)
                .thenReturn(2L);
        when(dormRepository.save(any(Dorm.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Room result1 = dormService.addRoom(mockDorm, mockRoom);
        Room result2 = dormService.addRoom(mockDorm, room2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(dormRepository, times(2)).save(any(Dorm.class));
    }
}