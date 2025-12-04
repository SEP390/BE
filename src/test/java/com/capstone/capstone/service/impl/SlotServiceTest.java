package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.checkin.GuardCheckinRequest;
import com.capstone.capstone.dto.request.slot.SwapSlotRequest;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDorm;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricing;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricingAndUser;
import com.capstone.capstone.dto.response.slot.SwapSlotResponse;
import com.capstone.capstone.dto.response.slotHistory.SlotHistoryResponse;
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
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {
    private final UUID USER_ID = UUID.randomUUID();
    private final UUID SLOT_ID_1 = UUID.randomUUID();
    private final UUID SLOT_ID_2 = UUID.randomUUID();
    private final UUID ROOM_ID_1 = UUID.randomUUID();
    private final UUID ROOM_ID_2 = UUID.randomUUID();
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private SlotHistoryRepository slotHistoryRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SemesterService semesterService;
    @Mock
    private InvoiceChangeService invoiceChangeService;
    @InjectMocks
    private SlotService slotService;
    private User mockUser;
    private Room mockRoom1;
    private Room mockRoom2;
    private Slot mockSlot1;
    private Slot mockSlot2;
    private RoomPricing mockPricing1;
    private RoomPricing mockPricing2;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(USER_ID);
        mockPricing1 = new RoomPricing();
        mockPricing1.setTotalSlot(4);
        mockPricing2 = new RoomPricing();
        mockPricing2.setTotalSlot(6);
        mockRoom1 = new Room();
        mockRoom1.setId(ROOM_ID_1);
        mockRoom1.setRoomNumber("A101");
        mockRoom1.setTotalSlot(4);
        mockRoom1.setPricing(mockPricing1);
        mockRoom1.setStatus(StatusRoomEnum.AVAILABLE);
        mockRoom2 = new Room();
        mockRoom2.setId(ROOM_ID_2);
        mockRoom2.setRoomNumber("B202");
        mockRoom2.setTotalSlot(6);
        mockRoom2.setPricing(mockPricing2);
        mockRoom2.setStatus(StatusRoomEnum.AVAILABLE);
        mockSlot1 = new Slot();
        mockSlot1.setId(SLOT_ID_1);
        mockSlot1.setRoom(mockRoom1);
        mockSlot1.setSlotName("A101_1");
        mockSlot1.setStatus(StatusSlotEnum.AVAILABLE);
        mockSlot1.setUser(mockUser);
        mockSlot2 = new Slot();
        mockSlot2.setId(SLOT_ID_2);
        mockSlot2.setRoom(mockRoom2);
        mockSlot2.setSlotName("B202_1");
        mockSlot2.setStatus(StatusSlotEnum.AVAILABLE);
    }

    @Test
    void save_ShouldReturnSavedSlot() {
        when(slotRepository.save(mockSlot1)).thenReturn(mockSlot1);
        Slot result = slotService.save(mockSlot1);
        assertThat(result).isEqualTo(mockSlot1);
        verify(slotRepository).save(mockSlot1);
    }

    @Test
    void getByUser_ShouldReturnSlot_WhenFound() {
        when(slotRepository.findByUser(mockUser)).thenReturn(mockSlot1);
        Optional<Slot> result = slotService.getByUser(mockUser);
        assertThat(result).isPresent().contains(mockSlot1);
    }

    @Test
    void getByUser_ShouldReturnEmpty_WhenNotFound() {
        when(slotRepository.findByUser(mockUser)).thenReturn(null);
        Optional<Slot> result = slotService.getByUser(mockUser);
        assertThat(result).isEmpty();
    }

    @Test
    void getByRoom_ShouldReturnListOfSlots() {
        List<Slot> slots = List.of(mockSlot1, mockSlot2);
        when(slotRepository.findByRoom(mockRoom1)).thenReturn(slots);
        List<Slot> result = slotService.getByRoom(mockRoom1);
        assertThat(result).isEqualTo(slots);
    }

    @Test
    void getResponseById_ShouldReturnMappedSlotResponse_WhenFound() {
        SlotResponseJoinRoomAndDormAndPricingAndUser response = new SlotResponseJoinRoomAndDormAndPricingAndUser();
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.of(mockSlot1));
        when(modelMapper.map(mockSlot1, SlotResponseJoinRoomAndDormAndPricingAndUser.class)).thenReturn(response);
        SlotResponseJoinRoomAndDormAndPricingAndUser result = slotService.getResponseById(SLOT_ID_1);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void getResponseById_ShouldThrowException_WhenNotFound() {
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.getResponseById(SLOT_ID_1))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void create_ShouldGenerateCorrectNumberOfSlotsAndSaveThem() {
        int totalSlots = 3;
        mockRoom1.setTotalSlot(totalSlots);
        mockRoom1.setRoomNumber("C303");
        when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        List<Slot> createdSlots = slotService.create(mockRoom1);
        assertThat(createdSlots).hasSize(totalSlots);
        assertThat(createdSlots.get(0).getSlotName()).isEqualTo("C303_1");
        assertThat(createdSlots.get(1).getSlotName()).isEqualTo("C303_2");
        assertThat(createdSlots.get(2).getSlotName()).isEqualTo("C303_3");
        createdSlots.forEach(slot -> {
            assertThat(slot.getRoom()).isEqualTo(mockRoom1);
            assertThat(slot.getStatus()).isEqualTo(StatusSlotEnum.AVAILABLE);
        });
        verify(slotRepository).saveAll(anyList());
    }

    @Test
    void deleteByRoom_ShouldCallDeleteAllByRoom() {
        slotService.deleteByRoom(mockRoom1);
        verify(slotRepository).deleteAllByRoom(mockRoom1);
        verifyNoMoreInteractions(slotRepository);
    }

    @Test
    void lock_ShouldUpdateSlotStatusAndUser_AndNotUpdateRoomStatus_WhenRoomNotFull() {
        mockSlot1.setStatus(StatusSlotEnum.AVAILABLE);
        mockSlot1.setUser(null);
        when(slotRepository.save(any(Slot.class))).thenReturn(mockSlot1);
        when(roomRepository.isFull(mockRoom1)).thenReturn(false);
        slotService.lock(mockSlot1, mockUser);
        ArgumentCaptor<Slot> slotCaptor = ArgumentCaptor.forClass(Slot.class);
        verify(slotRepository).save(slotCaptor.capture());
        Slot savedSlot = slotCaptor.getValue();
        assertThat(savedSlot.getStatus()).isEqualTo(StatusSlotEnum.LOCK);
        assertThat(savedSlot.getUser()).isEqualTo(mockUser);
        verify(roomRepository).isFull(mockRoom1);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void lock_ShouldUpdateSlotStatusAndUser_AndUpdateRoomStatusToFull_WhenRoomIsFull() {
        mockSlot1.setStatus(StatusSlotEnum.AVAILABLE);
        mockSlot1.setUser(null);
        when(slotRepository.save(any(Slot.class))).thenReturn(mockSlot1);
        when(roomRepository.isFull(mockRoom1)).thenReturn(true);
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom1);
        slotService.lock(mockSlot1, mockUser);
        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        Room savedRoom = roomCaptor.getValue();
        assertThat(savedRoom.getStatus()).isEqualTo(StatusRoomEnum.FULL);
        verify(roomRepository).isFull(mockRoom1);
    }

    @Test
    void unlock_ShouldUpdateSlotStatusAndUserToNull_AndNotUpdateRoomStatus_WhenRoomIsFull() {
        mockSlot1.setStatus(StatusSlotEnum.LOCK);
        mockRoom1.setStatus(StatusRoomEnum.FULL);
        when(slotRepository.save(any(Slot.class))).thenReturn(mockSlot1);
        when(roomRepository.isFull(mockRoom1)).thenReturn(true);
        slotService.unlock(mockSlot1);
        ArgumentCaptor<Slot> slotCaptor = ArgumentCaptor.forClass(Slot.class);
        verify(slotRepository).save(slotCaptor.capture());
        Slot savedSlot = slotCaptor.getValue();
        assertThat(savedSlot.getStatus()).isEqualTo(StatusSlotEnum.AVAILABLE);
        assertThat(savedSlot.getUser()).isNull();
        verify(roomRepository).isFull(mockRoom1);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void unlock_ShouldUpdateSlotStatusAndUserToNull_AndUpdateRoomStatusToAvailable_WhenRoomNotFull() {
        mockSlot1.setStatus(StatusSlotEnum.LOCK);
        mockRoom1.setStatus(StatusRoomEnum.FULL);
        when(slotRepository.save(any(Slot.class))).thenReturn(mockSlot1);
        when(roomRepository.isFull(mockRoom1)).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom1);
        slotService.unlock(mockSlot1);
        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        Room savedRoom = roomCaptor.getValue();
        assertThat(savedRoom.getStatus()).isEqualTo(StatusRoomEnum.AVAILABLE);
        verify(roomRepository).isFull(mockRoom1);
    }

    @Test
    void getCurrent_Case1_Success_SlotIsAvailable() {
        mockSlot1.setStatus(StatusSlotEnum.UNAVAILABLE);
        SlotResponseJoinRoomAndDormAndPricing expectedResponse = new SlotResponseJoinRoomAndDormAndPricing();
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(slotRepository.findByUser(mockUser)).thenReturn(mockSlot1);
            when(modelMapper.map(mockSlot1, SlotResponseJoinRoomAndDormAndPricing.class)).thenReturn(expectedResponse);
            SlotResponseJoinRoomAndDormAndPricing result = slotService.getCurrent();
            assertThat(result).isEqualTo(expectedResponse);
            verify(invoiceRepository, never()).findLatestBookingInvoice(any());
        }
    }

    @Test
    void getCurrent_Case2_Success_SlotIsNull() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(slotRepository.findByUser(mockUser)).thenReturn(null);
            SlotResponseJoinRoomAndDormAndPricing result = slotService.getCurrent();
            assertThat(result).isNull();
            verify(modelMapper, never()).map(any(), any());
        }
    }

    @Test
    void getCurrent_Case3_SlotLock_NoInvoiceOrPayment_ShouldUnlockSlot() {
        mockSlot1.setStatus(StatusSlotEnum.LOCK);
        mockSlot1.setUser(mockUser);
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(slotRepository.findByUser(mockUser)).thenReturn(mockSlot1);
            when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.empty());
            SlotResponseJoinRoomAndDormAndPricing result = slotService.getCurrent();
            assertThat(result).isNull();
            ArgumentCaptor<Slot> slotCaptor = ArgumentCaptor.forClass(Slot.class);
            verify(slotRepository).save(slotCaptor.capture());
            assertThat(slotCaptor.getValue().getStatus()).isEqualTo(StatusSlotEnum.AVAILABLE);
            assertThat(slotCaptor.getValue().getUser()).isNull();
            verify(paymentRepository, never()).findLatestByInvoice(any());
        }
    }

    @Test
    void getCurrent_Case4_SlotLock_PaymentCancelled_ShouldUnlockSlot() {
        mockSlot1.setStatus(StatusSlotEnum.LOCK);
        mockSlot1.setUser(mockUser);
        Invoice mockInvoice = new Invoice();
        Payment mockPayment = new Payment();
        mockPayment.setInvoice(mockInvoice);
        mockPayment.setStatus(PaymentStatus.CANCEL);
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(slotRepository.findByUser(mockUser)).thenReturn(mockSlot1);
            when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(mockInvoice));
            when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
            SlotResponseJoinRoomAndDormAndPricing result = slotService.getCurrent();
            assertThat(result).isNull();
            ArgumentCaptor<Slot> slotCaptor = ArgumentCaptor.forClass(Slot.class);
            verify(slotRepository).save(slotCaptor.capture());
            assertThat(slotCaptor.getValue().getStatus()).isEqualTo(StatusSlotEnum.AVAILABLE);
            assertThat(slotCaptor.getValue().getUser()).isNull();
            verify(invoiceRepository, never()).save(any());
        }
    }

    @Test
    void getCurrent_Case5_SlotLock_InvoiceExpired_ShouldCancelPaymentAndInvoiceAndUnlockSlot() {
        mockSlot1.setStatus(StatusSlotEnum.LOCK);
        mockSlot1.setUser(mockUser);
        Invoice mockInvoice = new Invoice();
        mockInvoice.setStatus(PaymentStatus.PENDING);
        mockInvoice.setExpireTime(LocalDateTime.now().minusHours(1));
        Payment mockPayment = new Payment();
        mockPayment.setInvoice(mockInvoice);
        mockPayment.setStatus(PaymentStatus.PENDING);
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(slotRepository.findByUser(mockUser)).thenReturn(mockSlot1);
            when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(mockInvoice));
            when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(mockInvoice);
            SlotResponseJoinRoomAndDormAndPricing result = slotService.getCurrent();
            assertThat(result).isNull();
            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(paymentCaptor.capture());
            assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.CANCEL);
            ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepository).save(invoiceCaptor.capture());
            assertThat(invoiceCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.CANCEL);
            verify(invoiceChangeService).onChange(mockInvoice);
            verify(slotRepository, never()).save(mockSlot1);
        }
    }

    @Test
    void getCurrent_Case6_SlotLock_InvoiceNotExpired_ShouldReturnMappedSlot() {
        mockSlot1.setStatus(StatusSlotEnum.LOCK);
        mockSlot1.setUser(mockUser);
        SlotResponseJoinRoomAndDormAndPricing expectedResponse = new SlotResponseJoinRoomAndDormAndPricing();
        Invoice mockInvoice = new Invoice();
        mockInvoice.setStatus(PaymentStatus.PENDING);
        mockInvoice.setExpireTime(LocalDateTime.now().plusHours(1));
        Payment mockPayment = new Payment();
        mockPayment.setInvoice(mockInvoice);
        mockPayment.setStatus(PaymentStatus.PENDING);
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(slotRepository.findByUser(mockUser)).thenReturn(mockSlot1);
            when(invoiceRepository.findLatestBookingInvoice(mockUser)).thenReturn(Optional.of(mockInvoice));
            when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
            when(modelMapper.map(mockSlot1, SlotResponseJoinRoomAndDormAndPricing.class)).thenReturn(expectedResponse);
            SlotResponseJoinRoomAndDormAndPricing result = slotService.getCurrent();
            assertThat(result).isEqualTo(expectedResponse);
            verify(paymentRepository, never()).save(any());
            verify(invoiceRepository, never()).save(any());
            verify(invoiceChangeService, never()).onChange(any());
        }
    }

    @Test
    void checkin_ShouldUpdateSlotStatusAndRecordCheckinHistory() {
        mockSlot1.setStatus(StatusSlotEnum.LOCK);
        mockUser.setUserCode("SV001");
        mockSlot1.setUser(mockUser);
        GuardCheckinRequest request = new GuardCheckinRequest(SLOT_ID_1, "");
        SlotHistory mockHistory = new SlotHistory();
        mockHistory.setCheckin(null);
        mockHistory.setSlotId(SLOT_ID_1);
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.of(mockSlot1));
        when(slotRepository.save(any(Slot.class))).thenReturn(mockSlot1);
        when(slotHistoryRepository.findCurrent(mockUser, SLOT_ID_1)).thenReturn(Optional.of(mockHistory));
        when(slotHistoryRepository.save(any(SlotHistory.class))).thenReturn(mockHistory);
        when(modelMapper.map(mockSlot1, SlotResponseJoinRoomAndDormAndPricingAndUser.class)).thenReturn(new SlotResponseJoinRoomAndDormAndPricingAndUser());
        slotService.checkin(request);
        ArgumentCaptor<Slot> slotCaptor = ArgumentCaptor.forClass(Slot.class);
        verify(slotRepository).save(slotCaptor.capture());
        assertThat(slotCaptor.getValue().getStatus()).isEqualTo(StatusSlotEnum.UNAVAILABLE);
        ArgumentCaptor<SlotHistory> historyCaptor = ArgumentCaptor.forClass(SlotHistory.class);
        verify(slotHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getCheckin()).isNotNull();
    }

    @Test
    void checkin_ShouldThrowException_WhenSlotNotFound() {
        GuardCheckinRequest request = new GuardCheckinRequest(SLOT_ID_1, "");
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.checkin(request))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void checkin_ShouldThrowException_WhenUserIsNull() {
        mockSlot1.setUser(null);
        GuardCheckinRequest request = new GuardCheckinRequest(SLOT_ID_1, "");
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.of(mockSlot1));
        assertThatThrownBy(() -> slotService.checkin(request))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void checkin_ShouldHandleNoCurrentHistoryFound() {
        mockSlot1.setStatus(StatusSlotEnum.LOCK);
        mockSlot1.setUser(mockUser);
        GuardCheckinRequest request = new GuardCheckinRequest(SLOT_ID_1, "");
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.of(mockSlot1));
        when(slotRepository.save(any(Slot.class))).thenReturn(mockSlot1);
        when(slotHistoryRepository.findCurrent(mockUser, SLOT_ID_1)).thenReturn(Optional.empty());
        when(modelMapper.map(mockSlot1, SlotResponseJoinRoomAndDormAndPricingAndUser.class)).thenReturn(new SlotResponseJoinRoomAndDormAndPricingAndUser());
        slotService.checkin(request);
        ArgumentCaptor<Slot> slotCaptor = ArgumentCaptor.forClass(Slot.class);
        verify(slotRepository).save(slotCaptor.capture());
        assertThat(slotCaptor.getValue().getStatus()).isEqualTo(StatusSlotEnum.UNAVAILABLE);
        verify(slotHistoryRepository, never()).save(any(SlotHistory.class));
    }

    @Test
    void checkout_ShouldUpdateSlotStatusAndRemoveUser_AndRecordCheckoutHistory() {
        mockSlot1.setStatus(StatusSlotEnum.UNAVAILABLE);
        mockSlot1.setUser(mockUser);
        mockSlot1.setId(SLOT_ID_1);
        SlotHistory mockHistory = new SlotHistory();
        mockHistory.setCheckout(null);
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.of(mockSlot1));
        when(slotRepository.save(any(Slot.class))).thenReturn(mockSlot1);
        when(slotHistoryRepository.findCurrent(mockUser, SLOT_ID_1)).thenReturn(Optional.of(mockHistory));
        when(slotHistoryRepository.save(any(SlotHistory.class))).thenReturn(mockHistory);
        when(modelMapper.map(mockSlot1, SlotResponseJoinRoomAndDormAndPricingAndUser.class)).thenReturn(new SlotResponseJoinRoomAndDormAndPricingAndUser());
        slotService.checkout(SLOT_ID_1);
        ArgumentCaptor<Slot> slotCaptor = ArgumentCaptor.forClass(Slot.class);
        verify(slotRepository).save(slotCaptor.capture());
        assertThat(slotCaptor.getValue().getStatus()).isEqualTo(StatusSlotEnum.AVAILABLE);
        assertThat(slotCaptor.getValue().getUser()).isNull();
        ArgumentCaptor<SlotHistory> historyCaptor = ArgumentCaptor.forClass(SlotHistory.class);
        verify(slotHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getCheckout()).isNotNull();
    }

    @Test
    void checkout_ShouldThrowException_WhenSlotNotFound() {
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.checkout(SLOT_ID_1))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void checkout_ShouldThrowException_WhenSlotIsEmpty() {
        mockSlot1.setUser(null);
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.of(mockSlot1));
        assertThatThrownBy(() -> slotService.checkout(SLOT_ID_1))
                .isInstanceOf(AppException.class)
                .hasMessage("SLOT_EMPTY");
    }

    @Test
    void getAll_ShouldReturnPagedModel_AndApplyFiltersCorrectly() {
        Map<String, Object> filter = Map.of(
                "userCode", "SV001",
                "userId", USER_ID,
                "status", StatusSlotEnum.UNAVAILABLE,
                "roomId", ROOM_ID_1
        );
        Pageable pageable = PageRequest.of(0, 10);
        List<Slot> slotList = List.of(mockSlot1);
        Page<Slot> slotPage = new PageImpl<>(slotList, pageable, 1);
        when(slotRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(slotPage);
        when(modelMapper.map(mockSlot1, SlotResponseJoinRoomAndDormAndPricingAndUser.class)).thenReturn(new SlotResponseJoinRoomAndDormAndPricingAndUser());
        PagedModel<SlotResponseJoinRoomAndDormAndPricingAndUser> result = slotService.getAll(filter, pageable);
        assertThat(result.getContent()).hasSize(1);
        verify(slotRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAll_ShouldHandleEmptyFilters() {
        Map<String, Object> filter = Collections.emptyMap();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Slot> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(slotRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
        PagedModel<SlotResponseJoinRoomAndDormAndPricingAndUser> result = slotService.getAll(filter, pageable);
        assertThat(result.getContent()).isEmpty();
        verify(slotRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void swap_Case1_Success_PricingEqual_NoInvoiceCreated() {
        mockUser.setSlot(mockSlot1);
        mockSlot1.setStatus(StatusSlotEnum.UNAVAILABLE);
        mockSlot2.setStatus(StatusSlotEnum.AVAILABLE);
        mockSlot2.getRoom().setPricing(mockPricing1);
        SwapSlotRequest request = new SwapSlotRequest(USER_ID, SLOT_ID_2);
        Semester mockSemester = new Semester();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(slotRepository.findById(SLOT_ID_2)).thenReturn(Optional.of(mockSlot2));
        when(semesterService.getCurrent()).thenReturn(Optional.of(mockSemester));
        when(slotRepository.save(any(Slot.class))).thenAnswer(i -> i.getArgument(0));
        when(slotHistoryRepository.save(any(SlotHistory.class))).thenAnswer(i -> i.getArgument(0));
        SlotHistory currentHistory = new SlotHistory();
        when(slotHistoryRepository.findCurrent(mockUser, SLOT_ID_1)).thenReturn(Optional.of(currentHistory));
        when(modelMapper.map(any(Slot.class), eq(SlotResponseJoinRoomAndDorm.class))).thenReturn(new SlotResponseJoinRoomAndDorm());
        when(modelMapper.map(any(SlotHistory.class), eq(SlotHistoryResponse.class))).thenReturn(new SlotHistoryResponse());
        SwapSlotResponse response = slotService.swap(request);
        assertThat(mockSlot2.getUser()).isEqualTo(mockUser);
        assertThat(mockSlot2.getStatus()).isEqualTo(StatusSlotEnum.UNAVAILABLE);
        assertThat(mockSlot1.getUser()).isNull();
        assertThat(mockSlot1.getStatus()).isEqualTo(StatusSlotEnum.AVAILABLE);
        assertThat(currentHistory.getCheckout()).isNotNull();
        verify(invoiceRepository, never()).save(any(Invoice.class));
        assertThat(response.getInvoice()).isNull();
        verify(slotHistoryRepository, times(1)).save(any(SlotHistory.class));
        verify(slotHistoryRepository, times(1)).save(currentHistory);
    }

    @Test
    void swap_Case2_Success_PricingHigher_InvoiceCreated() {
        mockUser.setSlot(mockSlot1);
        mockSlot1.setStatus(StatusSlotEnum.UNAVAILABLE);
        mockSlot2.setStatus(StatusSlotEnum.AVAILABLE);
        SwapSlotRequest request = new SwapSlotRequest(USER_ID, SLOT_ID_2);
        Semester mockSemester = new Semester();
        Invoice mockInvoice = new Invoice();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(slotRepository.findById(SLOT_ID_2)).thenReturn(Optional.of(mockSlot2));
        when(semesterService.getCurrent()).thenReturn(Optional.of(mockSemester));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(mockInvoice);
        when(modelMapper.map(mockInvoice, InvoiceResponse.class)).thenReturn(new InvoiceResponse());
        when(slotRepository.save(any(Slot.class))).thenAnswer(i -> i.getArgument(0));
        when(slotHistoryRepository.save(any(SlotHistory.class))).thenAnswer(i -> i.getArgument(0));
        SlotHistory currentHistory = new SlotHistory();
        when(slotHistoryRepository.findCurrent(mockUser, SLOT_ID_1)).thenReturn(Optional.of(currentHistory));
        when(modelMapper.map(any(Slot.class), eq(SlotResponseJoinRoomAndDorm.class))).thenReturn(new SlotResponseJoinRoomAndDorm());
        when(modelMapper.map(any(SlotHistory.class), eq(SlotHistoryResponse.class))).thenReturn(new SlotHistoryResponse());
        SwapSlotResponse response = slotService.swap(request);
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(invoiceCaptor.getValue().getType()).isEqualTo(InvoiceType.SWAP);
        assertThat(response.getInvoice()).isNotNull();
        verify(slotRepository, times(2)).save(any(Slot.class));
        verify(slotHistoryRepository, times(2)).save(any(SlotHistory.class));
    }

    @Test
    void swap_Case3_Exception_UserNotFound() {
        SwapSlotRequest request = new SwapSlotRequest(USER_ID, SLOT_ID_2);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.swap(request))
                .isInstanceOf(AppException.class)
                .hasMessage("USER_NOT_FOUND");
    }

    @Test
    void swap_Case4_Exception_CurrentSlotNotFound() {
        mockUser.setSlot(null);
        SwapSlotRequest request = new SwapSlotRequest(USER_ID, SLOT_ID_2);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        assertThatThrownBy(() -> slotService.swap(request))
                .isInstanceOf(AppException.class)
                .hasMessage("CURRENT_SLOT_NOT_FOUND");
    }

    @Test
    void swap_Case5_Exception_NewSlotNotFound() {
        mockUser.setSlot(mockSlot1);
        SwapSlotRequest request = new SwapSlotRequest(USER_ID, SLOT_ID_2);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(slotRepository.findById(SLOT_ID_2)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.swap(request))
                .isInstanceOf(AppException.class)
                .hasMessage("SLOT_NOT_FOUND");
    }

    @Test
    void swap_Case6_Exception_SlotLowerPrice() {
        mockUser.setSlot(mockSlot2);
        mockSlot1.getRoom().setPricing(mockPricing1);
        SwapSlotRequest request = new SwapSlotRequest(USER_ID, SLOT_ID_1);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.of(mockSlot1));
        assertThatThrownBy(() -> slotService.swap(request))
                .isInstanceOf(AppException.class)
                .hasMessage("SLOT_LOWER_PRICE");
    }

    @Test
    void swap_Case7_Exception_SameSlot() {
        mockUser.setSlot(mockSlot1);
        SwapSlotRequest request = new SwapSlotRequest(USER_ID, SLOT_ID_1);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(slotRepository.findById(SLOT_ID_1)).thenReturn(Optional.of(mockSlot1));
        assertThatThrownBy(() -> slotService.swap(request))
                .isInstanceOf(AppException.class)
                .hasMessage("SAME_SLOT");
    }

    @Test
    void swap_Case8_Exception_SemesterNotFound() {
        mockUser.setSlot(mockSlot1);
        mockSlot2.getRoom().setPricing(mockPricing1);
        SwapSlotRequest request = new SwapSlotRequest(USER_ID, SLOT_ID_2);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(slotRepository.findById(SLOT_ID_2)).thenReturn(Optional.of(mockSlot2));
        when(semesterService.getCurrent()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.swap(request))
                .isInstanceOf(AppException.class)
                .hasMessage("SEMESTER_NOT_FOUND");
    }
}