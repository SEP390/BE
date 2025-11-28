package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.invoice.CreateInvoiceRequest;
import com.capstone.capstone.dto.request.invoice.CreateInvoiceSubject;
import com.capstone.capstone.dto.request.invoice.CreateInvoiceRequest.UserId;
import com.capstone.capstone.dto.response.invoice.InvoiceCountResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponseJoinUser;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SemesterService semesterService;

    @Mock
    private EWUsageRepository ewUsageRepository;

    @Mock
    private EWPriceRepository eWPriceRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private SlotInvoiceRepository slotInvoiceRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private User testUser;
    private Invoice testInvoice;
    private Semester testSemester;
    private Room testRoom;
    private Slot testSlot;
    private EWPrice testEWPrice;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");

        testInvoice = new Invoice();
        testInvoice.setId(UUID.randomUUID());
        testInvoice.setUser(testUser);
        testInvoice.setPrice(1000L);
        testInvoice.setType(InvoiceType.BOOKING);
        testInvoice.setStatus(PaymentStatus.PENDING);
        testInvoice.setCreateTime(LocalDateTime.now());

        testSemester = new Semester();
        testSemester.setId(UUID.randomUUID());
        testSemester.setName("Fall 2024");
        testSemester.setStartDate(LocalDate.of(2024, 9, 1));
        testSemester.setEndDate(LocalDate.of(2024, 12, 31));

        testRoom = new Room();
        testRoom.setId(UUID.randomUUID());
        testRoom.setRoomNumber("101");
        RoomPricing pricing = new RoomPricing();
        pricing.setPrice(5000L);
        testRoom.setPricing(pricing);

        testSlot = new Slot();
        testSlot.setId(UUID.randomUUID());
        testSlot.setSlotName("A");
        testSlot.setRoom(testRoom);

        testEWPrice = new EWPrice();
        testEWPrice.setElectricPrice(3000L);
        testEWPrice.setWaterPrice(2000L);
        testEWPrice.setMaxElectricIndex(50);
        testEWPrice.setMaxWaterIndex(20);
    }

    @Test
    void create_ShouldCreateInvoiceSuccessfully() {
        // Arrange
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        Invoice result = invoiceService.create(testUser, 1000L, "Test reason", InvoiceType.BOOKING);

        // Assert
        assertNotNull(result);
        assertEquals(testInvoice.getId(), result.getId());
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void createElectricInvoice_WhenPendingInvoiceExists_ShouldNotCreateNewInvoice() {
        // Arrange
        when(invoiceRepository.exists(any(Specification.class))).thenReturn(true);

        // Act
        invoiceService.createElectricInvoice(testUser, testSemester, testEWPrice);

        // Assert
        verify(invoiceRepository, never()).save(any(Invoice.class));
        verify(ewUsageRepository, never()).containsPaid(any(), any(), any());
    }

    @Test
    void createElectricInvoice_WhenNoUsageExceedsLimit_ShouldNotCreateInvoice() {
        // Arrange
        when(invoiceRepository.exists(any(Specification.class))).thenReturn(false);
        when(ewUsageRepository.containsPaid(testUser, testSemester.getStartDate(), testSemester.getEndDate()))
                .thenReturn(false);

        EWUsage usage = new EWUsage();
        usage.setElectric(30); // Below limit of 50
        usage.setWater(10);    // Below limit of 20
        usage.setStartDate(LocalDate.of(2024, 9, 1));
        usage.setEndDate(LocalDate.of(2024, 9, 30));

        when(ewUsageRepository.findAllUnpaid(testUser, testSemester.getStartDate(), testSemester.getEndDate()))
                .thenReturn(List.of(usage));

        // Act
        invoiceService.createElectricInvoice(testUser, testSemester, testEWPrice);

        // Assert
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void createElectricInvoice_WhenUsageExceedsLimit_ShouldCreateInvoice() {
        // Arrange
        when(invoiceRepository.exists(any(Specification.class))).thenReturn(false);
        when(ewUsageRepository.containsPaid(testUser, testSemester.getStartDate(), testSemester.getEndDate()))
                .thenReturn(false);

        EWUsage usage = new EWUsage();
        usage.setElectric(60); // Exceeds limit of 50
        usage.setWater(25);    // Exceeds limit of 20
        usage.setStartDate(LocalDate.of(2024, 9, 1));
        usage.setEndDate(LocalDate.of(2024, 9, 30));

        when(ewUsageRepository.findAllUnpaid(testUser, testSemester.getStartDate(), testSemester.getEndDate()))
                .thenReturn(List.of(usage));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        invoiceService.createElectricInvoice(testUser, testSemester, testEWPrice);

        // Assert
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository, times(1)).save(invoiceCaptor.capture());

        Invoice savedInvoice = invoiceCaptor.getValue();
        // (60-50)*3000 + (25-20)*2000 = 10*3000 + 5*2000 = 40,000
        assertEquals(40000L, savedInvoice.getPrice());
        assertEquals(InvoiceType.EW, savedInvoice.getType());
    }

    @Test
    void createElectricInvoice_WhenContainsPaid_ShouldChargeFullAmount() {
        // Arrange
        when(invoiceRepository.exists(any(Specification.class))).thenReturn(false);
        when(ewUsageRepository.containsPaid(testUser, testSemester.getStartDate(), testSemester.getEndDate()))
                .thenReturn(true);

        EWUsage usage = new EWUsage();
        usage.setElectric(60);
        usage.setWater(25);
        usage.setStartDate(LocalDate.of(2024, 9, 1));
        usage.setEndDate(LocalDate.of(2024, 9, 30));

        when(ewUsageRepository.findAllUnpaid(testUser, testSemester.getStartDate(), testSemester.getEndDate()))
                .thenReturn(List.of(usage));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        invoiceService.createElectricInvoice(testUser, testSemester, testEWPrice);

        // Assert
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository, times(1)).save(invoiceCaptor.capture());

        Invoice savedInvoice = invoiceCaptor.getValue();
        // 60*3000 + 25*2000 = 230,000
        assertEquals(230000L, savedInvoice.getPrice());
    }

    @Test
    void create_WithRequest_WhenSubjectIsAll_ShouldCreateInvoiceForAllUsers() {
        // Arrange
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setSubject(CreateInvoiceSubject.ALL);
        request.setType(InvoiceType.OTHER);
        request.setPrice(5000L);
        request.setReason("Test reason");

        List<User> users = Arrays.asList(testUser, new User());

        when(semesterService.getCurrent()).thenReturn(Optional.of(testSemester));
        when(userRepository.findAll()).thenReturn(users);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        String result = invoiceService.create(request);

        // Assert
        assertEquals("SUCCESS", result);
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
    }

    @Test
    void create_WithRequest_WhenSubjectIsRoom_ShouldCreateInvoiceForRoomUsers() {
        // Arrange
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setSubject(CreateInvoiceSubject.ROOM);
        request.setRoomId(testRoom.getId());
        request.setType(InvoiceType.OTHER);
        request.setPrice(5000L);
        request.setReason("Test reason");

        List<User> users = List.of(testUser);

        when(semesterService.getCurrent()).thenReturn(Optional.of(testSemester));
        when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));
        when(roomRepository.findUsers(testRoom)).thenReturn(users);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        String result = invoiceService.create(request);

        // Assert
        assertEquals("SUCCESS", result);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void create_WithRequest_WhenSubjectIsUser_ShouldCreateInvoiceForSpecificUsers() {
        // Arrange
        UserId invoiceUser = new UserId();
        invoiceUser.setUserId(testUser.getId());

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setSubject(CreateInvoiceSubject.USER);
        request.setUsers(List.of(invoiceUser));
        request.setType(InvoiceType.OTHER);
        request.setPrice(5000L);
        request.setReason("Test reason");

        when(semesterService.getCurrent()).thenReturn(Optional.of(testSemester));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        String result = invoiceService.create(request);

        // Assert
        assertEquals("SUCCESS", result);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void create_WithRequest_WhenCurrentSemesterNotFound_ShouldThrowException() {
        // Arrange
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        when(semesterService.getCurrent()).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            invoiceService.create(request);
        });
        assertEquals("CURRENT_SEMESTER_NOT_FOUND", exception.getMessage());
    }

    @Test
    void create_WithRequest_WhenTypeIsEW_ShouldCreateElectricInvoices() {
        // Arrange
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setSubject(CreateInvoiceSubject.ALL);
        request.setType(InvoiceType.EW);

        when(semesterService.getCurrent()).thenReturn(Optional.of(testSemester));
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(eWPriceRepository.getCurrent()).thenReturn(Optional.of(testEWPrice));
        when(invoiceRepository.exists(any(Specification.class))).thenReturn(false);
        when(ewUsageRepository.containsPaid(any(), any(), any())).thenReturn(false);
        when(ewUsageRepository.findAllUnpaid(any(), any(), any())).thenReturn(Collections.emptyList());

        // Act
        String result = invoiceService.create(request);

        // Assert
        assertEquals("SUCCESS", result);
        verify(eWPriceRepository, times(1)).getCurrent();
    }

    @Test
    void getAllByUser_ShouldReturnPagedInvoices() {
        // Arrange
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(testUser);

            Map<String, Object> filter = new HashMap<>();
            Pageable pageable = PageRequest.of(0, 10);
            Page<Invoice> invoicePage = new PageImpl<>(List.of(testInvoice));
            InvoiceResponse invoiceResponse = new InvoiceResponse();

            when(invoiceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(invoicePage);
            when(modelMapper.map(any(Invoice.class), eq(InvoiceResponse.class))).thenReturn(invoiceResponse);

            // Act
            var result = invoiceService.getAllByUser(filter, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
        }
    }

    @Test
    void getAll_ShouldReturnPagedInvoicesWithUser() {
        // Arrange
        Map<String, Object> filter = new HashMap<>();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Invoice> invoicePage = new PageImpl<>(List.of(testInvoice));
        InvoiceResponseJoinUser invoiceResponse = new InvoiceResponseJoinUser();

        when(invoiceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(invoicePage);
        when(modelMapper.map(any(Invoice.class), eq(InvoiceResponseJoinUser.class))).thenReturn(invoiceResponse);

        // Act
        var result = invoiceService.getAll(filter, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void count_ShouldReturnInvoiceCountResponse() {
        // Arrange
        when(invoiceRepository.count()).thenReturn(100L);
        when(invoiceRepository.count(any(Specification.class))).thenReturn(60L, 40L);

        // Act
        InvoiceCountResponse result = invoiceService.count();

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getTotalCount());
        assertEquals(60L, result.getTotalSuccess());
        assertEquals(40L, result.getTotalPending());
    }

    @Test
    void authorize_WhenUserOwnsInvoice_ShouldReturnTrue() {
        // Arrange
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(testUser);
            when(invoiceRepository.exists(any(Specification.class))).thenReturn(true);

            // Act
            boolean result = invoiceService.authorize(testInvoice.getId());

            // Assert
            assertTrue(result);
        }
    }

    @Test
    void authorize_WhenUserDoesNotOwnInvoice_ShouldReturnFalse() {
        // Arrange
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(testUser);
            when(invoiceRepository.exists(any(Specification.class))).thenReturn(false);

            // Act
            boolean result = invoiceService.authorize(testInvoice.getId());

            // Assert
            assertFalse(result);
        }
    }

    @Test
    void create_WithSlot_ShouldCreateBookingInvoiceWithExpireTime() {
        // Arrange
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(slotInvoiceRepository.save(any(SlotInvoice.class))).thenReturn(new SlotInvoice());

        // Act
        Invoice result = invoiceService.create(testUser, testSlot, testSemester);

        // Assert
        assertNotNull(result);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
        verify(slotInvoiceRepository, times(1)).save(any(SlotInvoice.class));

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice savedInvoice = invoiceCaptor.getValue();

        assertEquals(5000L, savedInvoice.getPrice());
        assertEquals(InvoiceType.BOOKING, savedInvoice.getType());
        assertNotNull(savedInvoice.getExpireTime());
    }

    @Test
    void create_WithSlot_WhenPriceNotFound_ShouldThrowException() {
        // Arrange
        testSlot.setRoom(new Room()); // Room without pricing

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            invoiceService.create(testUser, testSlot, testSemester);
        });
        assertEquals("PRICE_NOT_FOUND", exception.getMessage());
    }

    @Test
    void userCount_ShouldReturnUserInvoiceCountResponse() {
        // Arrange
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(testUser);
            when(invoiceRepository.count(any(Specification.class))).thenReturn(50L, 30L, 20L);

            // Act
            InvoiceCountResponse result = invoiceService.userCount();

            // Assert
            assertNotNull(result);
            assertEquals(50L, result.getTotalCount());
            assertEquals(30L, result.getTotalSuccess());
            assertEquals(20L, result.getTotalPending());
        }
    }
}