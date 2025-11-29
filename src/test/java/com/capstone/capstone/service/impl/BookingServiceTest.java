package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private SlotRepository slotRepository;
    @Mock
    private SlotService slotService;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private SemesterService semesterService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private ScheduledExecutorService scheduledExecutorService;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private SlotInvoiceRepository slotInvoiceRepository;
    @Mock
    private VNPayService vnPayService;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private BookingValidateService bookingValidateService;

    @InjectMocks
    private BookingService bookingService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private User mockUser;
    private Slot mockSlot;
    private Room mockRoom;
    private Semester mockSemester;

    @BeforeEach
    void setUp() {
        // Mock Static SecurityUtils
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);

        // Setup common entities
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setGender(GenderEnum.MALE);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

        mockRoom = new Room();
        mockRoom.setId(UUID.randomUUID());
        mockRoom.setRoomNumber("101");

        mockSlot = new Slot();
        mockSlot.setId(UUID.randomUUID());
        mockSlot.setSlotName("101_1");
        mockSlot.setRoom(mockRoom);
        mockSlot.setStatus(StatusSlotEnum.AVAILABLE);

        mockSemester = new Semester();
        mockSemester.setId(UUID.randomUUID());
        mockSemester.setName("Spring 2025");
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    // =================================================================================================
    // METHOD: create(UUID slotId)
    // =================================================================================================

    @Test
    void create_Success() {
        UUID slotId = mockSlot.getId();

        // Mocks
        when(semesterService.getNext()).thenReturn(mockSemester);
        doNothing().when(bookingValidateService).validate();
        when(slotService.getByUser(mockUser)).thenReturn(Optional.empty()); // User has no slot
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(mockSlot));
        when(roomRepository.isValid(mockRoom, mockUser.getGender())).thenReturn(true);

        Invoice mockInvoice = new Invoice();
        when(invoiceService.create(mockUser, mockSlot, mockSemester)).thenReturn(mockInvoice);

        Payment mockPayment = new Payment();
        when(paymentService.create(mockInvoice)).thenReturn(mockPayment);
        when(paymentService.getPaymentUrl(mockPayment)).thenReturn("http://vnpay.url");

        // Execute
        String result = bookingService.create(slotId);

        // Verify
        assertEquals("http://vnpay.url", result);
        verify(slotService).lock(mockSlot, mockUser);
        verify(scheduledExecutorService).schedule(any(InvoiceExpireService.class), eq(10L), eq(TimeUnit.MINUTES));
    }

    @Test
    void create_Fail_NextSemesterNotFound() {
        UUID slotId = UUID.randomUUID();
        when(semesterService.getNext()).thenReturn(null);

        AppException ex = assertThrows(AppException.class, () -> bookingService.create(slotId));
        assertEquals("NEXT_SEMESTER_NOT_FOUND", ex.getMessage());
    }

    @Test
    void create_Fail_AlreadyBooked() {
        UUID slotId = UUID.randomUUID();
        when(semesterService.getNext()).thenReturn(mockSemester);
        doNothing().when(bookingValidateService).validate();

        // User already has a slot
        when(slotService.getByUser(mockUser)).thenReturn(Optional.of(new Slot()));

        AppException ex = assertThrows(AppException.class, () -> bookingService.create(slotId));
        assertEquals("ALREADY_BOOKED", ex.getMessage());
    }

    @Test
    void create_Fail_SlotNotFound() {
        UUID slotId = UUID.randomUUID();
        when(semesterService.getNext()).thenReturn(mockSemester);
        doNothing().when(bookingValidateService).validate();
        when(slotService.getByUser(mockUser)).thenReturn(Optional.empty());

        // Slot not in DB
        when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> bookingService.create(slotId));
        assertEquals("SLOT_NOT_FOUND", ex.getMessage());
    }

    @Test
    void create_Fail_SlotNotAvailable() {
        UUID slotId = mockSlot.getId();
        mockSlot.setStatus(StatusSlotEnum.LOCK); // Not AVAILABLE

        when(semesterService.getNext()).thenReturn(mockSemester);
        doNothing().when(bookingValidateService).validate();
        when(slotService.getByUser(mockUser)).thenReturn(Optional.empty());
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(mockSlot));

        AppException ex = assertThrows(AppException.class, () -> bookingService.create(slotId));
        assertEquals("SLOT_NOT_AVAILABLE", ex.getMessage());
    }

    @Test
    void create_Fail_GenderInvalid() {
        UUID slotId = mockSlot.getId();

        when(semesterService.getNext()).thenReturn(mockSemester);
        doNothing().when(bookingValidateService).validate();
        when(slotService.getByUser(mockUser)).thenReturn(Optional.empty());
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(mockSlot));
        // Gender mismatch
        when(roomRepository.isValid(mockRoom, mockUser.getGender())).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> bookingService.create(slotId));
        assertEquals("GENDER_INVALID", ex.getMessage());
    }

    // =================================================================================================
    // METHOD: cancel()
    // =================================================================================================

    @Test
    void cancel_Success_NoPaymentFound() {
        mockSlot.setStatus(StatusSlotEnum.LOCK);
        mockSlot.setUser(mockUser);

        when(slotService.getByUser(mockUser)).thenReturn(Optional.of(mockSlot));
        // Simulate no payment found
        when(paymentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(slotRepository.save(any(Slot.class))).thenAnswer(i -> i.getArguments()[0]);

        String result = bookingService.cancel();

        assertEquals("SUCCESS", result);
        assertEquals(StatusSlotEnum.AVAILABLE, mockSlot.getStatus());
        assertNull(mockSlot.getUser());
        verify(slotRepository).save(mockSlot);
    }

    @Test
    void cancel_Success_WithPaymentNotPending() {
        mockSlot.setStatus(StatusSlotEnum.LOCK);

        Invoice invoice = new Invoice();
        invoice.setStatus(PaymentStatus.SUCCESS); // Not PENDING

        Payment payment = new Payment();
        payment.setInvoice(invoice);

        when(slotService.getByUser(mockUser)).thenReturn(Optional.of(mockSlot));
        when(paymentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(payment));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);
        when(slotRepository.save(any(Slot.class))).thenAnswer(i -> i.getArguments()[0]);

        String result = bookingService.cancel();

        assertEquals("SUCCESS", result);
        assertEquals(PaymentStatus.CANCEL, invoice.getStatus());
        assertEquals(StatusSlotEnum.AVAILABLE, mockSlot.getStatus());
    }

    @Test
    void cancel_Fail_PaymentPending() {
        mockSlot.setStatus(StatusSlotEnum.LOCK);

        Invoice invoice = new Invoice();
        invoice.setStatus(PaymentStatus.PENDING); // Pending

        Payment payment = new Payment();
        payment.setInvoice(invoice);

        when(slotService.getByUser(mockUser)).thenReturn(Optional.of(mockSlot));
        when(paymentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(payment));

        AppException ex = assertThrows(AppException.class, () -> bookingService.cancel());
        assertEquals("PAYMENT_PENDING", ex.getMessage());
    }

    @Test
    void cancel_Fail_BookingNotFound() {
        mockSlot.setStatus(StatusSlotEnum.CHECKIN); // Not LOCK

        when(slotService.getByUser(mockUser)).thenReturn(Optional.of(mockSlot));

        AppException ex = assertThrows(AppException.class, () -> bookingService.cancel());
        assertEquals("BOOKING_NOT_FOUND", ex.getMessage());
    }

    @Test
    void cancel_Fail_NoSlotForUser() {
        when(slotService.getByUser(mockUser)).thenReturn(Optional.empty());
        // Since code calls .orElseThrow() without arguments on Optional, it throws NoSuchElementException
        assertThrows(java.util.NoSuchElementException.class, () -> bookingService.cancel());
    }


    // =================================================================================================
    // METHOD: payment()
    // =================================================================================================

    @Test
    void payment_Success() {
        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setStatus(PaymentStatus.PENDING);
        // Expire time in future
        invoice.setExpireTime(LocalDateTime.now().plusMinutes(5));

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setCreateTime(LocalDateTime.now());
        payment.setPrice(100000L);

        when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findLatestByInvoice(invoice)).thenReturn(Optional.of(payment));
        when(vnPayService.createPaymentUrl(payment.getId(), payment.getCreateTime(), payment.getPrice())).thenReturn("http://payment.url");

        String url = bookingService.payment();

        assertEquals("http://payment.url", url);
    }

    @Test
    void payment_Fail_InvoiceNotFound() {
        when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> bookingService.payment());
        assertEquals("INVOICE_NOT_FOUND", ex.getMessage());
    }

    @Test
    void payment_Fail_InvoiceAlreadyPaid() {
        Invoice invoice = new Invoice();
        invoice.setStatus(PaymentStatus.SUCCESS);

        when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(invoice));

        AppException ex = assertThrows(AppException.class, () -> bookingService.payment());
        assertEquals("INVOICE_ALREADY_PAID", ex.getMessage());
    }

    @Test
    void payment_Fail_InvoiceCancel() {
        Invoice invoice = new Invoice();
        invoice.setStatus(PaymentStatus.CANCEL);

        when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(invoice));

        AppException ex = assertThrows(AppException.class, () -> bookingService.payment());
        assertEquals("INVOICE_CANCEL", ex.getMessage());
    }

    @Test
    void payment_Fail_PaymentNotFound() {
        Invoice invoice = new Invoice();
        invoice.setStatus(PaymentStatus.PENDING);

        when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findLatestByInvoice(invoice)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> bookingService.payment());
        assertEquals("PAYMENT_NOT_FOUND", ex.getMessage());
    }

    @Test
    void payment_Fail_InvoiceExpired() {
        // Setup Invoice
        Invoice invoice = new Invoice();
        invoice.setStatus(PaymentStatus.PENDING);
        // Expired time
        invoice.setExpireTime(LocalDateTime.now().minusMinutes(1));

        SlotInvoice slotInvoice = new SlotInvoice();
        slotInvoice.setId(UUID.randomUUID());
        slotInvoice.setSlotId(mockSlot.getId());
        invoice.setSlotInvoice(slotInvoice);

        // Setup Payment
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);

        // Setup Slot
        mockSlot.setUser(mockUser);
        mockSlot.setStatus(StatusSlotEnum.LOCK);

        // Mocks
        when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findLatestByInvoice(invoice)).thenReturn(Optional.of(payment));

        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        when(paymentRepository.save(payment)).thenReturn(payment);

        when(slotInvoiceRepository.findById(slotInvoice.getId())).thenReturn(Optional.of(slotInvoice));
        when(slotRepository.findById(mockSlot.getId())).thenReturn(Optional.of(mockSlot));
        when(slotRepository.save(mockSlot)).thenReturn(mockSlot);

        // Execute & Assert
        AppException ex = assertThrows(AppException.class, () -> bookingService.payment());
        assertEquals("INVOICE_EXPIRED", ex.getMessage());

        // Verify status changes
        assertEquals(PaymentStatus.CANCEL, invoice.getStatus());
        assertEquals(PaymentStatus.CANCEL, payment.getStatus());
        assertEquals(StatusSlotEnum.AVAILABLE, mockSlot.getStatus());
        assertNull(mockSlot.getUser());
    }
}