package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.room.CreateRoomPricingRequest;
import com.capstone.capstone.dto.request.room.UpdateRoomPricingRequest;
import com.capstone.capstone.dto.response.room.RoomPricingResponse;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.RoomPricing;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.RoomPricingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomPricingServiceTest {
    private final UUID PRICING_ID = UUID.randomUUID();
    @Mock
    private RoomPricingRepository roomPricingRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private RoomPricingService roomPricingService;
    private RoomPricing mockPricing;
    private Room mockRoom;
    private Slot mockSlot;

    @BeforeEach
    void setUp() {
        mockPricing = new RoomPricing();
        mockPricing.setId(PRICING_ID);
        mockPricing.setTotalSlot(4);
        mockPricing.setPrice(1000000L);
        mockRoom = new Room();
        mockSlot = new Slot();
    }

    @Test
    void getAll_ShouldReturnAllPricings() {

        List<RoomPricing> allPricings = List.of(mockPricing, new RoomPricing());
        when(roomPricingRepository.findAll()).thenReturn(allPricings);
        List<RoomPricing> result = roomPricingService.getAll();
        assertThat(result).isEqualTo(allPricings);
        verify(roomPricingRepository).findAll();
    }

    @Test
    void getAll_WithTotalSlot_ShouldFilterAndMap() {

        Integer totalSlotFilter = 4;
        List<RoomPricing> filteredPricings = List.of(mockPricing);
        RoomPricingResponse expectedResponse = new RoomPricingResponse();

        when(roomPricingRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(filteredPricings);
        when(modelMapper.map(mockPricing, RoomPricingResponse.class)).thenReturn(expectedResponse);
        List<RoomPricingResponse> result = roomPricingService.getAll(totalSlotFilter);
        assertThat(result).containsExactly(expectedResponse);
        verify(roomPricingRepository).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "price")));
        verify(modelMapper).map(mockPricing, RoomPricingResponse.class);
    }

    @Test
    void getAll_WithoutTotalSlot_ShouldReturnAllSorted() {

        List<RoomPricing> allPricings = List.of(mockPricing);
        RoomPricingResponse expectedResponse = new RoomPricingResponse();
        when(roomPricingRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(allPricings);
        when(modelMapper.map(mockPricing, RoomPricingResponse.class)).thenReturn(expectedResponse);
        List<RoomPricingResponse> result = roomPricingService.getAll(null);
        assertThat(result).containsExactly(expectedResponse);
        verify(roomPricingRepository).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "price")));
        verify(modelMapper).map(mockPricing, RoomPricingResponse.class);
    }

    @Test
    void getByRoom_ShouldReturnPricing_WhenFound() {

        when(roomPricingRepository.findByRoom(mockRoom)).thenReturn(Optional.of(mockPricing));
        Optional<RoomPricing> result = roomPricingService.getByRoom(mockRoom);
        assertThat(result).isPresent().contains(mockPricing);
        verify(roomPricingRepository).findByRoom(mockRoom);
    }

    @Test
    void getBySlot_ShouldReturnPricing_WhenFound() {

        when(roomPricingRepository.findBySlot(mockSlot)).thenReturn(Optional.of(mockPricing));
        Optional<RoomPricing> result = roomPricingService.getBySlot(mockSlot);
        assertThat(result).isPresent().contains(mockPricing);
        verify(roomPricingRepository).findBySlot(mockSlot);
    }

    @Test
    void create_FromRequest_ShouldMapCreateAndReturnResponse() {

        CreateRoomPricingRequest request = new CreateRoomPricingRequest();
        request.setTotalSlot(6);
        request.setPrice(2000000L);
        RoomPricing newPricing = new RoomPricing();
        newPricing.setTotalSlot(6);
        newPricing.setPrice(2000000L);
        newPricing.setId(UUID.randomUUID());
        RoomPricingResponse expectedResponse = new RoomPricingResponse();
        when(modelMapper.map(request, RoomPricing.class)).thenReturn(newPricing);
        when(roomPricingRepository.findByTotalSlot(any())).thenReturn(Optional.empty());
        when(roomPricingRepository.save(any(RoomPricing.class))).thenReturn(newPricing);
        when(modelMapper.map(newPricing, RoomPricingResponse.class)).thenReturn(expectedResponse);
        RoomPricingResponse result = roomPricingService.create(request);
        assertThat(result).isEqualTo(expectedResponse);
        verify(modelMapper).map(request, RoomPricing.class);
        verify(roomPricingRepository).save(newPricing);
        verify(modelMapper).map(newPricing, RoomPricingResponse.class);
    }

    @Test
    void getOrCreate_ShouldReturnExistingPricing_WhenFound() {

        Integer totalSlot = 8;
        RoomPricing existingPricing = new RoomPricing();
        existingPricing.setTotalSlot(totalSlot);
        existingPricing.setPrice(3000000L);
        when(roomPricingRepository.findByTotalSlot(totalSlot)).thenReturn(Optional.of(existingPricing));
        RoomPricing result = roomPricingService.getOrCreate(totalSlot);
        assertThat(result).isEqualTo(existingPricing);
        verify(roomPricingRepository).findByTotalSlot(totalSlot);
        verify(roomPricingRepository, never()).save(any(RoomPricing.class));
    }

    @Test
    void getOrCreate_ShouldCreateNewPricingWithZeroPrice_WhenNotFound() {

        Integer totalSlot = 2;
        RoomPricing newPricing = new RoomPricing();
        newPricing.setTotalSlot(2);
        newPricing.setPrice(0L);
        when(roomPricingRepository.findByTotalSlot(totalSlot)).thenReturn(Optional.empty());
        when(roomPricingRepository.findByTotalSlot(any())).thenReturn(Optional.empty());
        when(roomPricingRepository.save(any(RoomPricing.class))).thenAnswer(i -> {
            RoomPricing saved = i.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        RoomPricing result = roomPricingService.getOrCreate(totalSlot);
        assertThat(result.getTotalSlot()).isEqualTo(totalSlot);
        assertThat(result.getPrice()).isZero();
        verify(roomPricingRepository).findByTotalSlot(totalSlot);
        verify(roomPricingRepository).save(any(RoomPricing.class));
    }

    @Test
    void create_ByTotalSlotAndPrice_ShouldCallInternalCreateAndReturnSavedPricing() {

        Integer totalSlot = 5;
        Long price = 1500000L;
        RoomPricing newPricing = new RoomPricing();
        newPricing.setTotalSlot(totalSlot);
        newPricing.setPrice(price);
        newPricing.setId(UUID.randomUUID());
        when(roomPricingRepository.findByTotalSlot(totalSlot)).thenReturn(Optional.empty());
        when(roomPricingRepository.save(any(RoomPricing.class))).thenReturn(newPricing);
        RoomPricing result = roomPricingService.create(totalSlot, price);
        assertThat(result).isEqualTo(newPricing);
        verify(roomPricingRepository).findByTotalSlot(totalSlot);
        verify(roomPricingRepository).save(any(RoomPricing.class));
    }

    @Test
    void create_ByRoomPricing_ShouldClearIdAndSave() {

        RoomPricing pricingToCreate = mockPricing;
        pricingToCreate.setId(UUID.randomUUID());
        when(roomPricingRepository.findByTotalSlot(mockPricing.getTotalSlot())).thenReturn(Optional.empty());
        when(roomPricingRepository.save(any(RoomPricing.class))).thenAnswer(i -> i.getArgument(0));
        RoomPricing result = roomPricingService.create(pricingToCreate);
        assertThat(result.getId()).isNull();
        verify(roomPricingRepository).findByTotalSlot(mockPricing.getTotalSlot());
        verify(roomPricingRepository).save(pricingToCreate);
    }

    @Test
    void create_ByRoomPricing_ShouldThrowException_WhenTotalSlotExists() {

        RoomPricing pricingToCreate = mockPricing;
        when(roomPricingRepository.findByTotalSlot(mockPricing.getTotalSlot())).thenReturn(Optional.of(new RoomPricing()));
        assertThatThrownBy(() -> roomPricingService.create(pricingToCreate))
                .isInstanceOf(AppException.class)
                .hasMessage("TOTAL_SLOT_EXISTED");
        verify(roomPricingRepository, never()).save(any(RoomPricing.class));
    }

    @Test
    void update_ShouldFindUpdatePriceSaveAndMap() {

        Long newPrice = 5000000L;
        UpdateRoomPricingRequest request = new UpdateRoomPricingRequest();
        request.setPrice(newPrice);
        RoomPricing updatedPricing = new RoomPricing();
        updatedPricing.setId(PRICING_ID);
        updatedPricing.setPrice(newPrice);
        RoomPricingResponse expectedResponse = new RoomPricingResponse();
        when(roomPricingRepository.findById(PRICING_ID)).thenReturn(Optional.of(mockPricing));
        when(roomPricingRepository.save(any(RoomPricing.class))).thenReturn(updatedPricing);
        when(modelMapper.map(updatedPricing, RoomPricingResponse.class)).thenReturn(expectedResponse);
        RoomPricingResponse result = roomPricingService.update(PRICING_ID, request);
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(mockPricing.getPrice()).isEqualTo(newPrice);
        verify(roomPricingRepository).findById(PRICING_ID);
        verify(roomPricingRepository).save(mockPricing);
        verify(modelMapper).map(updatedPricing, RoomPricingResponse.class);
    }

    @Test
    void update_ShouldThrowException_WhenPricingNotFound() {

        UpdateRoomPricingRequest request = new UpdateRoomPricingRequest();
        when(roomPricingRepository.findById(PRICING_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> roomPricingService.update(PRICING_ID, request))
                .isInstanceOf(AppException.class)
                .hasMessage("PRICING_NOT_FOUND");
        verify(roomPricingRepository, never()).save(any(RoomPricing.class));
    }

    @Test
    void getByTotalSlot_ShouldReturnPricing_WhenFound() {

        Integer totalSlot = 6;
        when(roomPricingRepository.findByTotalSlot(totalSlot)).thenReturn(Optional.of(mockPricing));
        Optional<RoomPricing> result = roomPricingService.getByTotalSlot(totalSlot);
        assertThat(result).isPresent().contains(mockPricing);
        verify(roomPricingRepository).findByTotalSlot(totalSlot);
    }

    @Test
    void getById_ShouldReturnMappedPricing_WhenFound() {

        RoomPricingResponse expectedResponse = new RoomPricingResponse();
        when(roomPricingRepository.findById(PRICING_ID)).thenReturn(Optional.of(mockPricing));
        when(modelMapper.map(mockPricing, RoomPricingResponse.class)).thenReturn(expectedResponse);
        RoomPricingResponse result = roomPricingService.getById(PRICING_ID);
        assertThat(result).isEqualTo(expectedResponse);
        verify(roomPricingRepository).findById(PRICING_ID);
        verify(modelMapper).map(mockPricing, RoomPricingResponse.class);
    }

    @Test
    void getById_ShouldThrowException_WhenPricingNotFound() {

        when(roomPricingRepository.findById(PRICING_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> roomPricingService.getById(PRICING_ID))
                .isInstanceOf(AppException.class)
                .hasMessage("PRICING_NOT_FOUND");
    }
}