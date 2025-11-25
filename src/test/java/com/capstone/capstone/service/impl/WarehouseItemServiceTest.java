package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.warehouseItem.CreateWarehouseItemRequest;
import com.capstone.capstone.dto.request.warehouseItem.UpdateWarehouseItemRequest;
import com.capstone.capstone.dto.response.warehouseItem.CreateWarehouseItemResponse;
import com.capstone.capstone.dto.response.warehouseItem.GetAllWarehouseItemResponse;
import com.capstone.capstone.dto.response.warehouseItem.UpdateWarehouseItemResponse;
import com.capstone.capstone.entity.WarehouseItem;
import com.capstone.capstone.repository.WarehouseItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseItemServiceTest {

    @Mock
    private WarehouseItemRepository warehouseItemRepository;

    @InjectMocks
    private WarehouseItemService warehouseItemService;

    private CreateWarehouseItemRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        validCreateRequest = new CreateWarehouseItemRequest();
        validCreateRequest.setItemName("Búa");
        validCreateRequest.setItemUnit("Cái");
    }

    // --------- createWarehouseItem ---------

    @Test
    void createWarehouseItem_shouldCreateSuccessfully_whenValidRequest() {
        // Act
        CreateWarehouseItemResponse response =
                warehouseItemService.createWarehouseItem(validCreateRequest);

        // Assert: repository.save được gọi 1 lần
        ArgumentCaptor<WarehouseItem> captor = ArgumentCaptor.forClass(WarehouseItem.class);
        verify(warehouseItemRepository, times(1)).save(captor.capture());

        WarehouseItem savedEntity = captor.getValue();
        assertEquals("Búa", savedEntity.getItemName());
        assertEquals("Cái", savedEntity.getUnit());
        assertEquals(0, savedEntity.getQuantity());

        // Response mapping đúng
        assertNotNull(response);
        assertEquals(savedEntity.getId(), response.getWarehouseItemId());
        assertEquals("Búa", response.getItemName());
        assertEquals("Cái", response.getItemUnit());
        assertEquals(0, response.getQuantity());
    }

    @Test
    void createWarehouseItem_shouldThrowException_whenItemNameIsNull() {
        validCreateRequest.setItemName(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> warehouseItemService.createWarehouseItem(validCreateRequest)
        );

        assertEquals("Item's name or item's unit cannot be null", ex.getMessage());
        verify(warehouseItemRepository, never()).save(any());
    }

    @Test
    void createWarehouseItem_shouldThrowException_whenItemNameIsEmpty() {
        validCreateRequest.setItemName("");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> warehouseItemService.createWarehouseItem(validCreateRequest)
        );

        assertEquals("Item's name or item's unit cannot be null", ex.getMessage());
        verify(warehouseItemRepository, never()).save(any());
    }

    @Test
    void createWarehouseItem_shouldThrowException_whenItemUnitIsNull() {
        validCreateRequest.setItemUnit(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> warehouseItemService.createWarehouseItem(validCreateRequest)
        );

        assertEquals("Item's name or item's unit cannot be null", ex.getMessage());
        verify(warehouseItemRepository, never()).save(any());
    }

    @Test
    void createWarehouseItem_shouldThrowException_whenItemUnitIsEmpty() {
        validCreateRequest.setItemUnit("");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> warehouseItemService.createWarehouseItem(validCreateRequest) // FIXED HERE
        );

        assertEquals("Item's name or item's unit cannot be null", ex.getMessage());
        verify(warehouseItemRepository, never()).save(any());
    }

    // --------- getAllWarehouseItem ---------

    @Test
    void getAllWarehouseItem_shouldReturnEmptyList_whenNoItemInDatabase() {
        when(warehouseItemRepository.findAll()).thenReturn(Collections.emptyList());

        List<GetAllWarehouseItemResponse> result = warehouseItemService.getAllWarehouseItem();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(warehouseItemRepository, times(1)).findAll();
    }

    @Test
    void getAllWarehouseItem_shouldReturnMappedList_whenItemsExist() {
        WarehouseItem item1 = new WarehouseItem();
        item1.setId(UUID.randomUUID());
        item1.setItemName("Búa");
        item1.setUnit("Cái");
        item1.setQuantity(10);

        WarehouseItem item2 = new WarehouseItem();
        item2.setId(UUID.randomUUID());
        item2.setItemName("Đinh");
        item2.setUnit("Hộp");
        item2.setQuantity(50);

        when(warehouseItemRepository.findAll()).thenReturn(List.of(item1, item2));

        List<GetAllWarehouseItemResponse> result = warehouseItemService.getAllWarehouseItem();

        assertEquals(2, result.size());

        GetAllWarehouseItemResponse res1 = result.get(0);
        assertEquals(item1.getId(), res1.getWarehouseItemId());
        assertEquals("Búa", res1.getItemName());
        assertEquals("Cái", res1.getItemUnit());
        assertEquals(10, res1.getQuantity());

        GetAllWarehouseItemResponse res2 = result.get(1);
        assertEquals(item2.getId(), res2.getWarehouseItemId());
        assertEquals("Đinh", res2.getItemName());
        assertEquals("Hộp", res2.getItemUnit());
        assertEquals(50, res2.getQuantity());

        verify(warehouseItemRepository, times(1)).findAll();
    }

    // --------- updateWarehouseItem ---------

    @Test
    void updateWarehouseItem_shouldUpdateSuccessfully_whenIdExists() {
        UUID id = UUID.randomUUID();

        WarehouseItem existingItem = new WarehouseItem();
        existingItem.setId(id);
        existingItem.setItemName("Búa cũ");
        existingItem.setUnit("Cái");
        existingItem.setQuantity(5);

        UpdateWarehouseItemRequest updateRequest = new UpdateWarehouseItemRequest();
        updateRequest.setItemName("Búa mới");
        updateRequest.setItemUnit("Chiếc");

        when(warehouseItemRepository.findById(id)).thenReturn(Optional.of(existingItem));

        UpdateWarehouseItemResponse response =
                warehouseItemService.updateWarehouseItem(id, updateRequest);

        assertEquals("Búa mới", existingItem.getItemName());
        assertEquals("Chiếc", existingItem.getUnit());
        verify(warehouseItemRepository, times(1)).save(existingItem);

        assertEquals(id, response.getWarehouseItemId());
        assertEquals("Búa mới", response.getItemName());
        assertEquals("Chiếc", response.getItemUnit());
        assertEquals(5, response.getQuantity());
    }

    @Test
    void updateWarehouseItem_shouldThrowNoSuchElementException_whenIdNotFound() {
        UUID id = UUID.randomUUID();
        UpdateWarehouseItemRequest updateRequest = new UpdateWarehouseItemRequest();
        updateRequest.setItemName("Búa");
        updateRequest.setItemUnit("Cái");

        when(warehouseItemRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> warehouseItemService.updateWarehouseItem(id, updateRequest)
        );

        verify(warehouseItemRepository, never()).save(any());
    }
}