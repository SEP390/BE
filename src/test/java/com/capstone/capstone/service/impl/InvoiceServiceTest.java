package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.invoice.CreateInvoiceRequest;
import com.capstone.capstone.dto.request.invoice.CreateInvoiceSubject;
import com.capstone.capstone.dto.response.invoice.InvoiceCountResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponseJoinUser;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private MockedStatic<SecurityUtils> securityUtilsMock;
    private User mockUser;
    private Semester mockSemester;

    @BeforeEach
    void setUp() {
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockSemester = new Semester();
        mockSemester.setId(UUID.randomUUID());
        mockSemester.setStartDate(LocalDate.now().minusDays(30));
        mockSemester.setEndDate(LocalDate.now());
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void create_Basic_Success() {
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);
        Invoice result = invoiceService.create(mockUser, 100000L, "Test Fee", InvoiceType.OTHER);
        assertNotNull(result);
        assertEquals(100000L, result.getPrice());
        assertEquals("Test Fee", result.getReason());
        assertEquals(InvoiceType.OTHER, result.getType());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals(mockUser, result.getUser());
    }

    @Test
    void createElectricInvoice_Skip_IfPendingExists() {
        when(invoiceRepository.exists(any(Specification.class))).thenReturn(true);
        invoiceService.createElectricInvoice(mockUser, mockSemester, new EWPrice());
        verify(ewUsageRepository, never()).containsPaid(any(), any(), any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void create_Bulk_Success_SubjectAll_TypeOther() {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setSubject(CreateInvoiceSubject.ALL);
        request.setType(InvoiceType.OTHER);
        request.setPrice(50000L);
        request.setReason("General Fee");
        when(semesterService.getCurrent()).thenReturn(Optional.of(mockSemester));
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);
        String res = invoiceService.create(request);
        assertEquals("SUCCESS", res);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void create_Bulk_Success_SubjectRoom_TypeEW() {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setSubject(CreateInvoiceSubject.ROOM);
        request.setType(InvoiceType.EW);
        request.setRoomId(UUID.randomUUID());
        Room mockRoom = new Room();
        EWPrice mockPrice = new EWPrice();
        when(semesterService.getCurrent()).thenReturn(Optional.of(mockSemester));
        when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(mockRoom));
        when(roomRepository.findUsers(mockRoom)).thenReturn(List.of(mockUser));
        when(eWPriceRepository.getCurrent()).thenReturn(Optional.of(mockPrice));
        when(invoiceRepository.exists(any(Specification.class))).thenReturn(true);
        String res = invoiceService.create(request);
        assertEquals("SUCCESS", res);
        verify(eWPriceRepository).getCurrent();
    }

    @Test
    void create_Bulk_Success_SubjectUser() {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setSubject(CreateInvoiceSubject.USER);
        request.setType(InvoiceType.VIOLATION);
        CreateInvoiceRequest.UserId userIdObj = new CreateInvoiceRequest.UserId();
        userIdObj.setUserId(mockUser.getId());
        request.setUsers(List.of(userIdObj));
        request.setPrice(10L);
        when(semesterService.getCurrent()).thenReturn(Optional.of(mockSemester));
        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(new Invoice());
        String res = invoiceService.create(request);
        assertEquals("SUCCESS", res);
        verify(userRepository).findById(mockUser.getId());
    }

    @Test
    void create_Bulk_Fail_NoSemester() {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        when(semesterService.getCurrent()).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> invoiceService.create(request));
        assertEquals("CURRENT_SEMESTER_NOT_FOUND", ex.getMessage());
    }

    @Test
    void create_Bulk_Fail_EWPriceNotFound() {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setSubject(CreateInvoiceSubject.ALL);
        request.setType(InvoiceType.EW);
        when(semesterService.getCurrent()).thenReturn(Optional.of(mockSemester));
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        when(eWPriceRepository.getCurrent()).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> invoiceService.create(request));
        assertEquals("EW_PRICE_NOT_FOUND", ex.getMessage());
    }

    @Test
    void create_Booking_Success() {
        Slot mockSlot = new Slot();
        mockSlot.setId(UUID.randomUUID());
        mockSlot.setSlotName("101_1");
        Room mockRoom = new Room();
        mockRoom.setRoomNumber("101");
        RoomPricing pricing = new RoomPricing();
        pricing.setPrice(1000000L);
        mockRoom.setPricing(pricing);
        mockSlot.setRoom(mockRoom);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> {
            Invoice inv = (Invoice) i.getArguments()[0];
            inv.setId(UUID.randomUUID());
            return inv;
        });
        when(slotInvoiceRepository.save(any(SlotInvoice.class))).thenAnswer(i -> i.getArguments()[0]);
        Invoice result = invoiceService.create(mockUser, mockSlot, mockSemester);
        assertNotNull(result);
        assertEquals(1000000L, result.getPrice());
        assertEquals(InvoiceType.BOOKING, result.getType());
        assertNotNull(result.getExpireTime());
        verify(slotInvoiceRepository).save(any(SlotInvoice.class));
    }

    @Test
    void create_Booking_Fail_PriceNotFound() {
        Slot mockSlot = new Slot();
        Room mockRoom = new Room();
        mockRoom.setPricing(null);
        mockSlot.setRoom(mockRoom);
        AppException ex = assertThrows(AppException.class, () -> invoiceService.create(mockUser, mockSlot, mockSemester));
        assertEquals("PRICE_NOT_FOUND", ex.getMessage());
    }

    @Test
    void getAllByUser_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        Pageable pageable = PageRequest.of(0, 10);
        when(invoiceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(new Invoice())));
        when(modelMapper.map(any(), eq(InvoiceResponse.class))).thenReturn(new InvoiceResponse());
        var result = invoiceService.getAllByUser(new HashMap<>(), pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(invoiceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(new Invoice())));
        when(modelMapper.map(any(), eq(InvoiceResponseJoinUser.class))).thenReturn(new InvoiceResponseJoinUser());
        var result = invoiceService.getAll(new HashMap<>(), pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void count_Success() {
        when(invoiceRepository.count()).thenReturn(10L);
        when(invoiceRepository.count(any(Specification.class))).thenReturn(5L);
        InvoiceCountResponse response = invoiceService.count();
        assertEquals(10L, response.getTotalCount());
        assertEquals(5L, response.getTotalSuccess());
        assertEquals(5L, response.getTotalPending());
    }

    @Test
    void authorize_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        UUID invoiceId = UUID.randomUUID();
        when(invoiceRepository.exists(any(Specification.class))).thenReturn(true);
        boolean auth = invoiceService.authorize(invoiceId);
        assertTrue(auth);
    }

    @Test
    void userCount_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
        when(invoiceRepository.count(any(Specification.class))).thenReturn(2L);
        InvoiceCountResponse response = invoiceService.userCount();
        assertEquals(2L, response.getTotalCount());
        assertEquals(2L, response.getTotalSuccess());
        assertEquals(2L, response.getTotalPending());
    }
}