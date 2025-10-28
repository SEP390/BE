package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.booking.CreateBookingRequest;
import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit Test
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    BookingService bookingService;

    @Mock
    SlotService slotService;
    @Mock
    PaymentService paymentService;
    @Mock
    RoomService roomService;
    @Mock
    ModelMapper modelMapper;

    /**
     * Success
     */
    @Test
    void create_Success() {
        Slot slot;
        User user;
        slot = new Slot();
        slot.setId(UUID.randomUUID());
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        user = new User();
        user.setId(UUID.randomUUID());
        Payment payment = new Payment();
        String paymentUrl = "paymentUrl";
        when(slotService.getById(slot.getId())).thenReturn(Optional.of(slot));
        when(paymentService.create(user, slot)).thenReturn(payment);
        when(paymentService.hasBooking(user, slot)).thenReturn(false);
        when(paymentService.createPaymentUrl(payment)).thenReturn(paymentUrl);
        doNothing().when(roomService).lockSlot(slot, user);
        MockedStatic<SecurityUtils> mockedStatic = Mockito.mockStatic(SecurityUtils.class);
        mockedStatic.when(SecurityUtils::getCurrentUser).thenReturn(user);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setSlotId(slot.getId());
        var response = bookingService.create(request);
        assertThat(response.getPaymentUrl()).isEqualTo(paymentUrl);

        verify(roomService, times(1)).lockSlot(slot, user);
        verify(paymentService, times(1)).create(user, slot);
        verify(paymentService, times(1)).createPaymentUrl(payment);
        mockedStatic.close();
    }

    /**
     * Error: Slot not found
     */
    @Test
    void create_SlotNotFound() {
        Slot slot;
        User user;
        slot = new Slot();
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        slot.setId(UUID.randomUUID());
        user = new User();
        user.setId(UUID.randomUUID());
        when(slotService.getById(slot.getId())).thenReturn(Optional.empty());

        MockedStatic<SecurityUtils> mockedStatic = Mockito.mockStatic(SecurityUtils.class);
        mockedStatic.when(SecurityUtils::getCurrentUser).thenReturn(user);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setSlotId(slot.getId());
        assertThatThrownBy(() -> bookingService.create(request)).isInstanceOf(AppException.class).hasMessage("SLOT_NOT_FOUND");

        verifyNoInteractions(roomService);
        verifyNoInteractions(paymentService);
        mockedStatic.close();
    }

    /**
     * Error: Slot already booked
     */
    @Test
    void create_AlreadyBooked() {
        Slot slot;
        User user;
        slot = new Slot();
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        slot.setId(UUID.randomUUID());
        user = new User();
        user.setId(UUID.randomUUID());
        Payment payment = new Payment();
        when(slotService.getById(slot.getId())).thenReturn(Optional.of(slot));
        when(paymentService.hasBooking(user, slot)).thenReturn(true);

        MockedStatic<SecurityUtils> mockedStatic = Mockito.mockStatic(SecurityUtils.class);
        mockedStatic.when(SecurityUtils::getCurrentUser).thenReturn(user);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setSlotId(slot.getId());
        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessage("ALREADY_BOOKED");

        verifyNoInteractions(roomService);
        verify(paymentService, never()).create(user, slot);
        verify(paymentService, never()).createPaymentUrl(payment);
        mockedStatic.close();
    }

    /**
     * Error: Slot not available
     */
    @Test
    void create_SlotNotAvailable() {
        Slot slot;
        User user;
        slot = new Slot();
        slot.setId(UUID.randomUUID());
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);
        user = new User();
        user.setId(UUID.randomUUID());

        when(slotService.getById(slot.getId())).thenReturn(Optional.of(slot));
        MockedStatic<SecurityUtils> mockedStatic = Mockito.mockStatic(SecurityUtils.class);
        mockedStatic.when(SecurityUtils::getCurrentUser).thenReturn(user);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setSlotId(slot.getId());
        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessage("SLOT_NOT_AVAILABLE");

        verifyNoInteractions(roomService);
        verifyNoInteractions(paymentService);
        mockedStatic.close();
    }

    @Test
    void history() {
    }

    @Test
    void current() {
    }

    @Test
    void getLatestPendingUrl() {
    }
}