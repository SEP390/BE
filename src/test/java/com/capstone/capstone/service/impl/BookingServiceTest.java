package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private SurveySelectRepository surveySelectRepository;

    @Mock
    private SlotService slotService;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private SemesterService semesterService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TimeConfigService timeConfigService;

    @Mock
    private SlotHistoryService slotHistoryService;

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

    @InjectMocks
    private BookingService bookingService;

    private User mockUser;
    private Slot mockSlot;
    private Semester mockSemester;
    private TimeConfig mockTimeConfig;
    private Invoice mockInvoice;
    private Payment mockPayment;
    private Room mockRoom;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setGender(GenderEnum.MALE);

        mockRoom = new Room();
        mockRoom.setId(UUID.randomUUID());

        mockSlot = new Slot();
        mockSlot.setId(UUID.randomUUID());
        mockSlot.setStatus(StatusSlotEnum.AVAILABLE);
        mockSlot.setRoom(mockRoom);

        mockSemester = new Semester();
        mockSemester.setId(UUID.randomUUID());

        mockTimeConfig = new TimeConfig();
        mockTimeConfig.setStartBookingDate(LocalDate.now().minusDays(5));
        mockTimeConfig.setEndBookingDate(LocalDate.now().plusDays(5));
        mockTimeConfig.setStartExtendDate(LocalDate.now().minusDays(3));
        mockTimeConfig.setEndExtendDate(LocalDate.now().plusDays(3));

        mockInvoice = new Invoice();
        mockInvoice.setId(UUID.randomUUID());
        mockInvoice.setStatus(PaymentStatus.PENDING);
        mockInvoice.setExpireTime(LocalDateTime.now().plusMinutes(10));

        mockPayment = new Payment();
        mockPayment.setId(UUID.randomUUID());
        mockPayment.setInvoice(mockInvoice);
        mockPayment.setPrice(1000000L);
        mockPayment.setCreateTime(LocalDateTime.now());
    }

    @Test
    void create_Success_FirstTimeBooking() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            UUID slotId = mockSlot.getId();
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            when(semesterService.getNext()).thenReturn(mockSemester);
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(false);
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(true);
            when(slotService.getByUser(mockUser)).thenReturn(Optional.empty());
            when(slotRepository.findById(slotId)).thenReturn(Optional.of(mockSlot));
            when(roomRepository.isValid(mockRoom, mockUser.getGender())).thenReturn(true);
            when(invoiceService.create(mockUser, mockSlot, mockSemester)).thenReturn(mockInvoice);
            when(paymentService.create(mockInvoice)).thenReturn(mockPayment);
            when(paymentService.getPaymentUrl(mockPayment)).thenReturn("http://payment.url");

            // Act
            String result = bookingService.create(slotId);

            // Assert
            assertNotNull(result);
            assertEquals("http://payment.url", result);
            verify(slotService).lock(mockSlot, mockUser);
            verify(scheduledExecutorService).schedule(any(Runnable.class), eq(10L), eq(TimeUnit.MINUTES));
        }
    }

    @Test
    void create_Success_ExtendBooking() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            UUID slotId = mockSlot.getId();
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            when(semesterService.getNext()).thenReturn(mockSemester);
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(true);
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(true);
            when(slotService.getByUser(mockUser)).thenReturn(Optional.empty());
            when(slotRepository.findById(slotId)).thenReturn(Optional.of(mockSlot));
            when(roomRepository.isValid(mockRoom, mockUser.getGender())).thenReturn(true);
            when(invoiceService.create(mockUser, mockSlot, mockSemester)).thenReturn(mockInvoice);
            when(paymentService.create(mockInvoice)).thenReturn(mockPayment);
            when(paymentService.getPaymentUrl(mockPayment)).thenReturn("http://payment.url");

            // Act
            String result = bookingService.create(slotId);

            // Assert
            assertNotNull(result);
            assertEquals("http://payment.url", result);
        }
    }

    @Test
    void create_ThrowsException_NextSemesterNotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(semesterService.getNext()).thenReturn(null);

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.create(UUID.randomUUID()));
            assertEquals("NEXT_SEMESTER_NOT_FOUND", exception.getMessage());
        }
    }

    @Test
    void create_ThrowsException_TimeConfigNotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(semesterService.getNext()).thenReturn(mockSemester);
            when(timeConfigService.getCurrent()).thenReturn(Optional.empty());

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.create(UUID.randomUUID()));
            assertEquals("TIME_CONFIG_NOT_FOUND", exception.getMessage());
        }
    }

    @Test
    void create_ThrowsException_BookingDateNotStart() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            mockTimeConfig.setStartBookingDate(LocalDate.now().plusDays(5));
            mockTimeConfig.setEndBookingDate(LocalDate.now().plusDays(10));

            when(semesterService.getNext()).thenReturn(mockSemester);
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(false);

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.create(UUID.randomUUID()));
            assertEquals("BOOKING_DATE_NOT_START", exception.getMessage());
        }
    }

    @Test
    void create_ThrowsException_SurveyNotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(semesterService.getNext()).thenReturn(mockSemester);
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(false);
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(false);

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.create(UUID.randomUUID()));
            assertEquals("SURVEY_NOT_FOUND", exception.getMessage());
        }
    }

    @Test
    void create_ThrowsException_AlreadyBooked() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(semesterService.getNext()).thenReturn(mockSemester);
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(false);
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(true);
            when(slotService.getByUser(mockUser)).thenReturn(Optional.of(mockSlot));

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.create(UUID.randomUUID()));
            assertEquals("ALREADY_BOOKED", exception.getMessage());
        }
    }

    @Test
    void create_ThrowsException_SlotNotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            UUID slotId = UUID.randomUUID();
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(semesterService.getNext()).thenReturn(mockSemester);
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(false);
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(true);
            when(slotService.getByUser(mockUser)).thenReturn(Optional.empty());
            when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.create(slotId));
            assertEquals("SLOT_NOT_FOUND", exception.getMessage());
        }
    }

    @Test
    void create_ThrowsException_SlotNotAvailable() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            UUID slotId = mockSlot.getId();
            mockSlot.setStatus(StatusSlotEnum.LOCK);
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            when(semesterService.getNext()).thenReturn(mockSemester);
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(false);
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(true);
            when(slotService.getByUser(mockUser)).thenReturn(Optional.empty());
            when(slotRepository.findById(slotId)).thenReturn(Optional.of(mockSlot));

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.create(slotId));
            assertEquals("SLOT_NOT_AVAILABLE", exception.getMessage());
        }
    }

    @Test
    void create_ThrowsException_GenderInvalid() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            UUID slotId = mockSlot.getId();
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            when(semesterService.getNext()).thenReturn(mockSemester);
            when(timeConfigService.getCurrent()).thenReturn(Optional.of(mockTimeConfig));
            when(slotHistoryService.existsByUser(mockUser)).thenReturn(false);
            when(surveySelectRepository.exists(any(Specification.class))).thenReturn(true);
            when(slotService.getByUser(mockUser)).thenReturn(Optional.empty());
            when(slotRepository.findById(slotId)).thenReturn(Optional.of(mockSlot));
            when(roomRepository.isValid(mockRoom, mockUser.getGender())).thenReturn(false);

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.create(slotId));
            assertEquals("GENDER_INVALID", exception.getMessage());
        }
    }

    @Test
    void cancel_Success_WithNoPayment() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockSlot.setStatus(StatusSlotEnum.LOCK);
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(slotService.getByUser(mockUser)).thenReturn(Optional.of(mockSlot));
            when(paymentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
            when(slotRepository.save(any(Slot.class))).thenReturn(mockSlot);

            // Act
            String result = bookingService.cancel();

            // Assert
            assertEquals("SUCCESS", result);
            ArgumentCaptor<Slot> slotCaptor = ArgumentCaptor.forClass(Slot.class);
            verify(slotRepository).save(slotCaptor.capture());
            assertEquals(StatusSlotEnum.AVAILABLE, slotCaptor.getValue().getStatus());
            assertNull(slotCaptor.getValue().getUser());
        }
    }

    @Test
    void cancel_Success_WithCancelledPayment() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockSlot.setStatus(StatusSlotEnum.LOCK);
            mockInvoice.setStatus(PaymentStatus.SUCCESS);
            mockPayment.setInvoice(mockInvoice);

            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(slotService.getByUser(mockUser)).thenReturn(Optional.of(mockSlot));
            when(paymentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockPayment));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(mockInvoice);
            when(slotRepository.save(any(Slot.class))).thenReturn(mockSlot);

            // Act
            String result = bookingService.cancel();

            // Assert
            assertEquals("SUCCESS", result);
            verify(invoiceRepository).save(any(Invoice.class));
            verify(slotRepository).save(any(Slot.class));
        }
    }

    @Test
    void cancel_ThrowsException_PaymentPending() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockSlot.setStatus(StatusSlotEnum.LOCK);
            mockInvoice.setStatus(PaymentStatus.PENDING);
            mockPayment.setInvoice(mockInvoice);

            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(slotService.getByUser(mockUser)).thenReturn(Optional.of(mockSlot));
            when(paymentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockPayment));

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.cancel());
            assertEquals("PAYMENT_PENDING", exception.getMessage());
        }
    }

    @Test
    void cancel_ThrowsException_BookingNotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockSlot.setStatus(StatusSlotEnum.AVAILABLE);
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(slotService.getByUser(mockUser)).thenReturn(Optional.of(mockSlot));

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.cancel());
            assertEquals("BOOKING_NOT_FOUND", exception.getMessage());
        }
    }

    @Test
    void payment_Success() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(mockInvoice));
            when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
            when(vnPayService.createPaymentUrl(any(UUID.class), any(LocalDateTime.class), anyLong()))
                    .thenReturn("http://vnpay.url");

            // Act
            String result = bookingService.payment();

            // Assert
            assertNotNull(result);
            assertEquals("http://vnpay.url", result);
            verify(vnPayService).createPaymentUrl(mockPayment.getId(), mockPayment.getCreateTime(), mockPayment.getPrice());
        }
    }

    @Test
    void payment_ThrowsException_InvoiceNotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.empty());

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.payment());
            assertEquals("INVOICE_NOT_FOUND", exception.getMessage());
        }
    }

    @Test
    void payment_ThrowsException_InvoiceAlreadyPaid() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockInvoice.setStatus(PaymentStatus.SUCCESS);
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(mockInvoice));

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.payment());
            assertEquals("INVOICE_ALREADY_PAID", exception.getMessage());
        }
    }

    @Test
    void payment_ThrowsException_InvoiceCancel() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockInvoice.setStatus(PaymentStatus.CANCEL);
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(mockInvoice));

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.payment());
            assertEquals("INVOICE_CANCEL", exception.getMessage());
        }
    }

    @Test
    void payment_ThrowsException_PaymentNotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(mockInvoice));
            when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.empty());

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.payment());
            assertEquals("PAYMENT_NOT_FOUND", exception.getMessage());
        }
    }

    @Test
    void payment_ThrowsException_InvoiceExpired() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockInvoice.setExpireTime(LocalDateTime.now().minusMinutes(5));
            SlotInvoice slotInvoice = new SlotInvoice();
            slotInvoice.setId(UUID.randomUUID());
            slotInvoice.setSlotId(mockSlot.getId());
            mockInvoice.setSlotInvoice(slotInvoice);

            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(mockInvoice));
            when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(mockInvoice);
            when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
            when(slotInvoiceRepository.findById(slotInvoice.getId())).thenReturn(Optional.of(slotInvoice));
            when(slotRepository.findById(mockSlot.getId())).thenReturn(Optional.of(mockSlot));
            when(slotRepository.save(any(Slot.class))).thenReturn(mockSlot);

            // Act & Assert
            AppException exception = assertThrows(AppException.class,
                    () -> bookingService.payment());
            assertEquals("INVOICE_EXPIRED", exception.getMessage());

            // Verify cleanup operations
            verify(invoiceRepository).save(any(Invoice.class));
            verify(paymentRepository).save(any(Payment.class));
            verify(slotRepository).save(any(Slot.class));
        }
    }
}