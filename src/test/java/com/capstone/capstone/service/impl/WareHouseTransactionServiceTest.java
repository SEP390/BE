package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.enums.TransactionTypeEnum;
import com.capstone.capstone.dto.request.warehouseTransaction.CreateWarehouseTransactionRequest;
import com.capstone.capstone.dto.response.warehouseTransaction.CreateWarehouseTransactionResponse;
import com.capstone.capstone.dto.response.warehouseTransaction.GetAllWarehouseTransactionResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.entity.WarehouseItem;
import com.capstone.capstone.entity.WarehouseTransaction;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.repository.WarehouseItemRepository;
import com.capstone.capstone.repository.WarehouseTransactionRepository;
import com.capstone.capstone.util.AuthenUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WareHouseTransactionServiceTest {

    @Mock
    private WarehouseTransactionRepository warehouseTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WarehouseItemRepository warehouseItemRepository;

    @InjectMocks
    private WareHouseTransactionService wareHouseTransactionService;

    private MockedStatic<AuthenUtil> authenUtilMock;

    private UUID userId;
    private UUID itemId;
    private User technicalUser;
    private WarehouseItem item;

    @BeforeEach
    void setUp() {
        authenUtilMock = mockStatic(AuthenUtil.class);

        userId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        technicalUser = new User();
        technicalUser.setId(userId);
        technicalUser.setFullName("Technical User");
        technicalUser.setRole(RoleEnum.TECHNICAL);

        item = new WarehouseItem();
        item.setId(itemId);
        item.setItemName("Búa");
        item.setUnit("Cái");
        item.setQuantity(10); // số lượng ban đầu trong kho
    }

    @AfterEach
    void tearDown() {
        authenUtilMock.close();
    }

    // ------------------------------------------------------------------------
    // createWarehouseTransaction
    // ------------------------------------------------------------------------

    @Test
    void createWarehouseTransaction_shouldCreateImportTransaction_andIncreaseQuantity() {
        // Arrange
        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(technicalUser));
        when(warehouseItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(warehouseTransactionRepository.save(any(WarehouseTransaction.class)))
                .thenAnswer(invocation -> {
                    WarehouseTransaction t = invocation.getArgument(0);
                    if (t.getId() == null) {
                        t.setId(UUID.randomUUID());
                    }
                    return t;
                });

        CreateWarehouseTransactionRequest req = new CreateWarehouseTransactionRequest();
        req.setItemId(itemId);
        req.setTransactionType(TransactionTypeEnum.IMPORT);
        req.setTransactionQuantity(5);
        req.setNote("Nhập thêm búa");
        req.setReportId(UUID.randomUUID());
        req.setRequestId(UUID.randomUUID());

        // Act
        CreateWarehouseTransactionResponse res =
                wareHouseTransactionService.createWarehouseTransaction(req);

        // Assert – quantity phải tăng 10 -> 15 (logic thực tế)
        assertEquals(15, item.getQuantity());

        // check transaction được save đúng
        ArgumentCaptor<WarehouseTransaction> captor =
                ArgumentCaptor.forClass(WarehouseTransaction.class);
        verify(warehouseTransactionRepository, times(1)).save(captor.capture());
        WarehouseTransaction saved = captor.getValue();

        assertEquals(technicalUser, saved.getUser());
        assertEquals(item, saved.getItem());
        assertEquals(TransactionTypeEnum.IMPORT, saved.getType());
        assertEquals(5, saved.getQuantity());
        assertEquals("Nhập thêm búa", saved.getNote());
        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));

        // mapping response
        assertNotNull(res);
        assertEquals(saved.getId(), res.getId());
        assertEquals(saved.getRequestId(), res.getRequestId());
        assertEquals(saved.getReportId(), res.getReportId());
        assertEquals(technicalUser.getId(), res.getActionById());
        assertEquals(technicalUser.getFullName(), res.getActionByName());
        assertEquals(saved.getCreatedAt(), res.getCreatedAt());
        assertEquals(TransactionTypeEnum.IMPORT, res.getTransactionType());
        assertEquals(5, res.getTransactionQuantity());
        assertEquals("Nhập thêm búa", res.getNote());
    }

    @Test
    void createWarehouseTransaction_shouldCreateExportTransaction_andDecreaseQuantity_whenEnoughStock() {
        // Arrange
        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(technicalUser));
        when(warehouseItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(warehouseTransactionRepository.save(any(WarehouseTransaction.class)))
                .thenAnswer(invocation -> {
                    WarehouseTransaction t = invocation.getArgument(0);
                    t.setId(UUID.randomUUID());
                    return t;
                });

        CreateWarehouseTransactionRequest req = new CreateWarehouseTransactionRequest();
        req.setItemId(itemId);
        req.setTransactionType(TransactionTypeEnum.EXPORT);
        req.setTransactionQuantity(4);
        req.setNote("Xuất búa cho phòng 101");

        // Act
        CreateWarehouseTransactionResponse res =
                wareHouseTransactionService.createWarehouseTransaction(req);

        // Assert – quantity phải giảm 10 -> 6
        assertEquals(6, item.getQuantity());
        assertEquals(TransactionTypeEnum.EXPORT, res.getTransactionType());
        assertEquals(4, res.getTransactionQuantity());
    }

    @Test
    void createWarehouseTransaction_shouldThrowException_whenExportQuantityGreaterThanStock() {
        // Arrange
        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(technicalUser));

        item.setQuantity(3); // tồn kho 3
        when(warehouseItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        CreateWarehouseTransactionRequest req = new CreateWarehouseTransactionRequest();
        req.setItemId(itemId);
        req.setTransactionType(TransactionTypeEnum.EXPORT);
        req.setTransactionQuantity(5); // xuất 5 > 3


        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> wareHouseTransactionService.createWarehouseTransaction(req),
                "Export > stock should throw exception instead of allowing negative quantity"
        );

        // Sau này khi bạn fix code, có thể assert thêm message:
         assertEquals("Not enough quantity in stock", ex.getMessage());

        // Không được save transaction khi bị lỗi
        verify(warehouseTransactionRepository, never()).save(any());
    }

    @Test
    void createWarehouseTransaction_shouldThrowException_whenCurrentUserNotFound() {
        // Arrange
        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CreateWarehouseTransactionRequest req = new CreateWarehouseTransactionRequest();
        req.setItemId(itemId);
        req.setTransactionType(TransactionTypeEnum.IMPORT);
        req.setTransactionQuantity(5);

        // Act + Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> wareHouseTransactionService.createWarehouseTransaction(req)
        );
        assertEquals("User not found", ex.getMessage());

        verify(warehouseItemRepository, never()).findById(any());
        verify(warehouseTransactionRepository, never()).save(any());
    }

    @Test
    void createWarehouseTransaction_shouldThrowException_whenItemNotFound() {
        // Arrange
        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(technicalUser));
        when(warehouseItemRepository.findById(itemId)).thenReturn(Optional.empty());

        CreateWarehouseTransactionRequest req = new CreateWarehouseTransactionRequest();
        req.setItemId(itemId);
        req.setTransactionType(TransactionTypeEnum.IMPORT);
        req.setTransactionQuantity(5);

        // Act + Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> wareHouseTransactionService.createWarehouseTransaction(req)
        );
        assertEquals("Item not found", ex.getMessage());

        verify(warehouseTransactionRepository, never()).save(any());
    }

    @Test
    void createWarehouseTransaction_shouldThrowException_whenUserRoleIsNotTechnical() {
        // Arrange
        User manager = new User();
        manager.setId(userId);
        manager.setFullName("Manager");
        manager.setRole(RoleEnum.MANAGER); // không phải TECHNICAL

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(manager));
        when(warehouseItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        CreateWarehouseTransactionRequest req = new CreateWarehouseTransactionRequest();
        req.setItemId(itemId);
        req.setTransactionType(TransactionTypeEnum.IMPORT);
        req.setTransactionQuantity(5);

        // Act + Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> wareHouseTransactionService.createWarehouseTransaction(req)
        );
        assertEquals("Role not allowed", ex.getMessage());

        // Không được thay đổi quantity và không save transaction
        assertEquals(10, item.getQuantity());
        verify(warehouseTransactionRepository, never()).save(any());
    }

    // ------------------------------------------------------------------------
    // getAllTransactions
    // ------------------------------------------------------------------------

    @Test
    void getAllTransactions_shouldReturnOnlyTechnicalUserTransactions_whenRoleTechnical() {
        // Arrange
        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(technicalUser));

        WarehouseTransaction t1 = new WarehouseTransaction();
        t1.setId(UUID.randomUUID());
        t1.setUser(technicalUser);
        t1.setItem(item);
        t1.setType(TransactionTypeEnum.IMPORT);
        t1.setQuantity(5);
        t1.setNote("Nhập");
        t1.setCreatedAt(LocalDateTime.now());

        WarehouseTransaction t2 = new WarehouseTransaction();
        t2.setId(UUID.randomUUID());
        t2.setUser(technicalUser);
        t2.setItem(item);
        t2.setType(TransactionTypeEnum.EXPORT);
        t2.setQuantity(2);
        t2.setNote("Xuất");
        t2.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(warehouseTransactionRepository.findAllByUser(technicalUser))
                .thenReturn(List.of(t1, t2));

        // Act
        List<GetAllWarehouseTransactionResponse> result =
                wareHouseTransactionService.getAllTransactions();

        // Assert
        assertEquals(2, result.size());

        GetAllWarehouseTransactionResponse r1 = result.get(0);
        assertEquals(t1.getId(), r1.getId());
        assertEquals(t1.getRequestId(), r1.getRequestId());
        assertEquals(t1.getReportId(), r1.getReportId());
        assertEquals(technicalUser.getId(), r1.getActionById());
        assertEquals(technicalUser.getFullName(), r1.getActionByName());
        assertEquals(t1.getCreatedAt(), r1.getCreatedAt());
        assertEquals(t1.getType(), r1.getTransactionType());
        assertEquals(t1.getQuantity(), r1.getTransactionQuantity());
        assertEquals(t1.getNote(), r1.getNote());

        verify(warehouseTransactionRepository, times(1)).findAllByUser(technicalUser);
        verify(warehouseTransactionRepository, never()).findAll();
    }

    @Test
    void getAllTransactions_shouldReturnAllTransactions_whenRoleManager() {
        // Arrange
        User manager = new User();
        manager.setId(userId);
        manager.setFullName("Manager");
        manager.setRole(RoleEnum.MANAGER);

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(manager));

        WarehouseTransaction t1 = new WarehouseTransaction();
        t1.setId(UUID.randomUUID());
        t1.setUser(technicalUser);
        t1.setItem(item);
        t1.setType(TransactionTypeEnum.IMPORT);
        t1.setQuantity(5);
        t1.setNote("Nhập");
        t1.setCreatedAt(LocalDateTime.now());

        WarehouseTransaction t2 = new WarehouseTransaction();
        t2.setId(UUID.randomUUID());
        t2.setUser(technicalUser);
        t2.setItem(item);
        t2.setType(TransactionTypeEnum.EXPORT);
        t2.setQuantity(2);
        t2.setNote("Xuất");
        t2.setCreatedAt(LocalDateTime.now().minusHours(3));

        when(warehouseTransactionRepository.findAll()).thenReturn(List.of(t1, t2));

        // Act
        List<GetAllWarehouseTransactionResponse> result =
                wareHouseTransactionService.getAllTransactions();

        // Assert
        assertEquals(2, result.size());
        verify(warehouseTransactionRepository, times(1)).findAll();
        verify(warehouseTransactionRepository, never()).findAllByUser(any());
    }

    /**
     * Logic thực tế (gợi ý): các role khác TECHNICAL / MANAGER không nên được xem giao dịch.
     * Hiện tại code của bạn trả về list rỗng, không throw. Nếu sau này bạn thêm Role khác
     * vào RoleEnum và update service để throw exception, hãy bổ sung thêm test case cho nhánh đó.
     */
}