package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.invoice.UpdateInvoiceRequest;
import com.capstone.capstone.dto.response.invoice.InvoiceResponseJoinUser;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceChangeServiceTest {
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private SlotHistoryRepository slotHistoryRepository;
    @Mock
    private EWUsageRepository ewUsageRepository;
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private InvoiceChangeService invoiceChangeService;
    private Invoice mockInvoice;
    private User mockUser;
    private Slot mockSlot;
    private SlotInvoice mockSlotInvoice;
    private Semester mockSemester;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockSemester = new Semester();
        mockSemester.setId(UUID.randomUUID());
        mockSemester.setStartDate(LocalDate.now().minusMonths(1));
        mockSemester.setEndDate(LocalDate.now().plusMonths(1));
        mockSlot = new Slot();
        mockSlot.setId(UUID.randomUUID());
        mockSlot.setSlotName("101_1");
        mockSlot.setUser(mockUser);
        mockSlot.setStatus(StatusSlotEnum.LOCK);
        mockSlot.setRoom(new Room());
        mockSlotInvoice = new SlotInvoice();
        mockSlotInvoice.setSlotId(mockSlot.getId());
        mockSlotInvoice.setSemester(mockSemester);
        mockInvoice = new Invoice();
        mockInvoice.setId(UUID.randomUUID());
        mockInvoice.setUser(mockUser);
        mockInvoice.setSlotInvoice(mockSlotInvoice);
    }

    @Test
    void onChange_Success_Booking_SlotFound() {
        mockInvoice.setStatus(PaymentStatus.SUCCESS);
        mockInvoice.setType(InvoiceType.BOOKING);
        when(slotRepository.findById(mockSlot.getId())).thenReturn(Optional.of(mockSlot));
        when(slotRepository.save(any(Slot.class))).thenAnswer(i -> i.getArguments()[0]);
        Invoice result = invoiceChangeService.onChange(mockInvoice);
        assertEquals(StatusSlotEnum.CHECKIN, mockSlot.getStatus());
        verify(slotRepository).save(mockSlot);
        ArgumentCaptor<SlotHistory> historyCaptor = ArgumentCaptor.forClass(SlotHistory.class);
        verify(slotHistoryRepository).save(historyCaptor.capture());
        SlotHistory history = historyCaptor.getValue();
        assertEquals(mockSlot.getId(), history.getSlotId());
        assertEquals(mockUser, history.getUser());
        assertEquals(mockSemester, history.getSemester());
        assertEquals(mockInvoice, result);
    }

    @Test
    void onChange_Success_Booking_SlotNotFound() {
        mockInvoice.setStatus(PaymentStatus.SUCCESS);
        mockInvoice.setType(InvoiceType.BOOKING);
        when(slotRepository.findById(mockSlot.getId())).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> invoiceChangeService.onChange(mockInvoice));
        assertEquals("SLOT_NOT_FOUND", ex.getMessage());
        verify(slotHistoryRepository, never()).save(any());
    }

    @Test
    void onChange_Success_EW_Payment() {
        mockInvoice.setStatus(PaymentStatus.SUCCESS);
        mockInvoice.setType(InvoiceType.EW);
        EWUsage usage1 = new EWUsage();
        usage1.setPaid(false);
        EWUsage usage2 = new EWUsage();
        usage2.setPaid(false);
        List<EWUsage> usages = List.of(usage1, usage2);
        when(semesterRepository.findCurrent()).thenReturn(mockSemester);
        when(ewUsageRepository.findAllUnpaid(mockUser, mockSemester.getStartDate(), mockSemester.getEndDate())).thenReturn(usages);
        invoiceChangeService.onChange(mockInvoice);
        assertTrue(usage1.getPaid());
        assertTrue(usage2.getPaid());
        verify(ewUsageRepository).saveAll(usages);
    }

    @Test
    void onChange_Cancel_Booking() {
        mockInvoice.setStatus(PaymentStatus.CANCEL);
        mockInvoice.setType(InvoiceType.BOOKING);
        mockSlot.setStatus(StatusSlotEnum.LOCK);
        when(invoiceRepository.save(mockInvoice)).thenReturn(mockInvoice);
        when(slotRepository.findById(mockSlot.getId())).thenReturn(Optional.of(mockSlot));
        when(slotRepository.save(any(Slot.class))).thenAnswer(i -> i.getArguments()[0]);
        invoiceChangeService.onChange(mockInvoice);
        assertEquals(PaymentStatus.CANCEL, mockInvoice.getStatus());
        verify(invoiceRepository).save(mockInvoice);
        assertEquals(StatusSlotEnum.AVAILABLE, mockSlot.getStatus());
        assertNull(mockSlot.getUser());
        verify(slotRepository).save(mockSlot);
    }

    @Test
    void onChange_Cancel_Booking_SlotNotFound() {
        mockInvoice.setStatus(PaymentStatus.CANCEL);
        mockInvoice.setType(InvoiceType.BOOKING);
        when(invoiceRepository.save(mockInvoice)).thenReturn(mockInvoice);
        when(slotRepository.findById(mockSlot.getId())).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> invoiceChangeService.onChange(mockInvoice));
    }

    @Test
    void update_Internal_Success() {
        mockInvoice.setStatus(PaymentStatus.PENDING);
        mockInvoice.setType(InvoiceType.BOOKING);
        PaymentStatus newStatus = PaymentStatus.SUCCESS;
        when(invoiceRepository.save(mockInvoice)).thenReturn(mockInvoice);
        when(slotRepository.findById(mockSlot.getId())).thenReturn(Optional.of(mockSlot));
        Invoice result = invoiceChangeService.update(mockInvoice, newStatus);
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        verify(invoiceRepository).save(mockInvoice);
        verify(slotHistoryRepository).save(any());
    }

    @Test
    void update_Internal_Fail_AlreadyPending() {
        mockInvoice.setStatus(PaymentStatus.PENDING);
        AppException ex = assertThrows(AppException.class, () -> invoiceChangeService.update(mockInvoice, PaymentStatus.PENDING));
        assertEquals("ALREADY_PENDING", ex.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void update_External_Success() {
        UUID invoiceId = mockInvoice.getId();
        UpdateInvoiceRequest request = new UpdateInvoiceRequest();
        request.setStatus(PaymentStatus.SUCCESS);
        mockInvoice.setStatus(PaymentStatus.PENDING);
        mockInvoice.setType(InvoiceType.BOOKING);
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(mockInvoice));
        when(invoiceRepository.save(mockInvoice)).thenReturn(mockInvoice);
        when(slotRepository.findById(mockSlot.getId())).thenReturn(Optional.of(mockSlot));
        InvoiceResponseJoinUser responseDTO = new InvoiceResponseJoinUser();
        when(modelMapper.map(mockInvoice, InvoiceResponseJoinUser.class)).thenReturn(responseDTO);
        InvoiceResponseJoinUser result = invoiceChangeService.update(invoiceId, request);
        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS, mockInvoice.getStatus());
    }

    @Test
    void update_External_Fail_NotFound() {
        UUID invoiceId = UUID.randomUUID();
        UpdateInvoiceRequest request = new UpdateInvoiceRequest();
        request.setStatus(PaymentStatus.SUCCESS);
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> invoiceChangeService.update(invoiceId, request));
    }
}