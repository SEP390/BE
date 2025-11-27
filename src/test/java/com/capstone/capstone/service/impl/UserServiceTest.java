package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.user.CreateUserRequest;
import com.capstone.capstone.dto.response.user.CreateAccountResponse;
import com.capstone.capstone.dto.response.user.GetAllResidentResponse;
import com.capstone.capstone.dto.response.user.GetUserByIdResponse;
import com.capstone.capstone.dto.response.user.GetUserInformationResponse;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IUploadService;
import com.capstone.capstone.util.AuthenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private IUploadService uploadService; // hiện tại chưa dùng, nhưng vẫn mock cho đủ deps

    @InjectMocks
    private UserService userService;

    // Helper tạo user giả
    private User buildUser(UUID id, String username, String email) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setDob(LocalDate.of(2000, 1, 1));
        u.setGender(GenderEnum.MALE);
        u.setRole(RoleEnum.RESIDENT);
        u.setFullName("Nguyen Van A");
        u.setPhoneNumber("0123456789");
        u.setUserCode("HE12345");
        u.setImage("avatar.png");
        return u;
    }

    // =========================================
    // createAccount()
    // =========================================

    // 🎯 TC1: Tạo account thành công khi username + email đều chưa tồn tại
    @Test
    void createAccount_shouldCreateUser_whenUsernameAndEmailAreUnique() {
        // Arrange
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("newuser");
        req.setPassword("123456");
        req.setEmail("new@fpt.edu.vn");
        req.setDob(LocalDate.of(2000, 1, 1));
        req.setGender(GenderEnum.MALE);
        req.setRole(RoleEnum.RESIDENT);

        // Không có user nào trong DB
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Password encoder hoạt động bình thường
        when(passwordEncoder.encode("123456")).thenReturn("encoded-pw");

        // Giả lập save trả về chính entity (thực tế Hibernate sẽ set id,... nhưng ở đây không cần thiết lắm)
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CreateAccountResponse resp = userService.createAccount(req);

        // Assert
        verify(userRepository, times(1)).findAll();
        verify(passwordEncoder, times(1)).encode("123456");
        verify(userRepository, times(1)).save(any(User.class));

        assertNotNull(resp);
        assertEquals("newuser", resp.getUsername());
        assertEquals("new@fpt.edu.vn", resp.getEmail());
        assertEquals(LocalDate.of(2000, 1, 1), resp.getDob());
    }

    // 🎯 TC2: Username đã tồn tại -> phải ném BadHttpRequestException, không tạo mới
    @Test
    void createAccount_shouldThrowException_whenUsernameAlreadyTaken() {
        // Arrange
        User existing = buildUser(UUID.randomUUID(), "existing", "exist@fpt.edu.vn");
        when(userRepository.findAll()).thenReturn(List.of(existing));

        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("existing"); // trùng username
        req.setEmail("new@fpt.edu.vn");
        req.setPassword("123");

        // Act + Assert
        BadHttpRequestException ex = assertThrows(
                BadHttpRequestException.class,
                () -> userService.createAccount(req)
        );

        assertEquals("Username is already taken", ex.getMessage());
        verify(userRepository, times(1)).findAll();
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    // 🎯 TC3: Email đã tồn tại -> phải ném BadHttpRequestException, không tạo mới
    @Test
    void createAccount_shouldThrowException_whenEmailAlreadyTaken() {
        // Arrange
        User existing = buildUser(UUID.randomUUID(), "user1", "exist@fpt.edu.vn");
        when(userRepository.findAll()).thenReturn(List.of(existing));

        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("newuser");
        req.setEmail("exist@fpt.edu.vn"); // trùng email
        req.setPassword("123");

        // Act + Assert
        BadHttpRequestException ex = assertThrows(
                BadHttpRequestException.class,
                () -> userService.createAccount(req)
        );

        assertEquals("Email is already taken", ex.getMessage());
        verify(userRepository, times(1)).findAll();
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    // 🎯 TC4 (logic thực tế): Password null -> BCryptPasswordEncoder ngoài thực tế sẽ ném IllegalArgumentException
    // Mình mock y chang behavior đó để test lộ bug (service không validate password null)
    @Test
    void createAccount_shouldThrowIllegalArgument_whenPasswordIsNull() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("userNullPw");
        req.setEmail("nullpw@fpt.edu.vn");
        req.setPassword(null);

        // Mô phỏng behavior của BCryptPasswordEncoder ngoài thực tế
        when(passwordEncoder.encode(null))
                .thenThrow(new IllegalArgumentException("rawPassword cannot be null"));

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createAccount(req)
        );

        assertEquals("rawPassword cannot be null", ex.getMessage());
        verify(userRepository, times(1)).findAll();
        verify(userRepository, never()).save(any());
    }

    // =========================================
    // getCurrentUserInformation()
    // =========================================

    // 🎯 TC5: Lấy thông tin current user thành công, user có slot
    @Test
    void getCurrentUserInformation_shouldReturnUserInfo_whenUserExistsAndHasSlot() {
        UUID currentUserId = UUID.randomUUID();
        User user = buildUser(currentUserId, "duongnt", "duong@fpt.edu.vn");

        Slot slot = new Slot();
        slot.setId(UUID.randomUUID());
        slot.setSlotName("A101");

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            // Mock static getCurrentUserId
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
            when(slotRepository.findByUser(user)).thenReturn(slot);

            // Act
            GetUserInformationResponse resp = userService.getCurrentUserInformation();

            // Assert
            verify(userRepository, times(1)).findById(currentUserId);
            verify(slotRepository, times(1)).findByUser(user);

            assertNotNull(resp);
            assertEquals("duongnt", resp.getUsername());
            assertEquals("duong@fpt.edu.vn", resp.getEmail());
            assertEquals("A101", resp.getSlotName());
            assertEquals(RoleEnum.RESIDENT, resp.getRole());
            assertEquals("avatar.png", resp.getImage());
        }
    }

    // 🎯 TC6: Lấy current user, user tồn tại nhưng không có slot -> slotName = null
    @Test
    void getCurrentUserInformation_shouldReturnNullSlotName_whenUserHasNoSlot() {
        UUID currentUserId = UUID.randomUUID();
        User user = buildUser(currentUserId, "duongnt", "duong@fpt.edu.vn");

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
            when(slotRepository.findByUser(user)).thenReturn(null);

            // Act
            GetUserInformationResponse resp = userService.getCurrentUserInformation();

            // Assert
            assertNotNull(resp);
            assertEquals("duongnt", resp.getUsername());
            assertNull(resp.getSlotName());
        }
    }

    // 🎯 TC7: Lấy current user nhưng userId không tồn tại -> NotFoundException
    @Test
    void getCurrentUserInformation_shouldThrowNotFound_whenUserNotFound() {
        UUID currentUserId = UUID.randomUUID();

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

            // Act + Assert
            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> userService.getCurrentUserInformation()
            );

            assertEquals("User not found", ex.getMessage());
            verify(slotRepository, never()).findByUser(any());
        }
    }

    // =========================================
    // getAllResidents()
    // =========================================

    // 🎯 TC8: Không có resident nào -> trả về list rỗng
    @Test
    void getAllResidents_shouldReturnEmptyList_whenNoResidentInDb() {
        // Arrange
        when(userRepository.findUserByRole(RoleEnum.RESIDENT))
                .thenReturn(Collections.emptyList());

        // Act
        List<GetAllResidentResponse> result = userService.getAllResidents();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findUserByRole(RoleEnum.RESIDENT);
        verify(slotRepository, never()).findByUser(any());
    }

    // 🎯 TC9: Có nhiều resident, một số có slot, một số không -> map đúng dữ liệu
    @Test
    void getAllResidents_shouldReturnMappedList_withAndWithoutSlot() {
        // Arrange
        User u1 = buildUser(UUID.randomUUID(), "res1", "res1@fpt.edu.vn");
        User u2 = buildUser(UUID.randomUUID(), "res2", "res2@fpt.edu.vn");

        when(userRepository.findUserByRole(RoleEnum.RESIDENT))
                .thenReturn(List.of(u1, u2));

        Slot slot1 = new Slot();
        slot1.setId(UUID.randomUUID());
        slot1.setSlotName("B202");

        when(slotRepository.findByUser(u1)).thenReturn(slot1);
        when(slotRepository.findByUser(u2)).thenReturn(null);

        // Act
        List<GetAllResidentResponse> result = userService.getAllResidents();

        // Assert
        assertEquals(2, result.size());

        GetAllResidentResponse r1 = result.get(0);
        assertEquals(u1.getId(), r1.getResidentId());
        assertEquals("res1", r1.getUserName());
        assertEquals("res1@fpt.edu.vn", r1.getEmail());
        assertEquals("B202", r1.getSlotName());

        GetAllResidentResponse r2 = result.get(1);
        assertEquals(u2.getId(), r2.getResidentId());
        assertEquals("res2", r2.getUserName());
        assertEquals("res2@fpt.edu.vn", r2.getEmail());
        assertNull(r2.getSlotName());

        verify(userRepository, times(1)).findUserByRole(RoleEnum.RESIDENT);
        verify(slotRepository, times(1)).findByUser(u1);
        verify(slotRepository, times(1)).findByUser(u2);
    }

    // =========================================
    // getUserById()
    // =========================================

    // 🎯 TC10: Lấy user theo id thành công -> map đủ field
    @Test
    void getUserById_shouldReturnUser_whenExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "sondeptrai", "son@fpt.edu.vn");
        user.setUserCode("HE160000");
        user.setPhoneNumber("0987654321");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Act
        GetUserByIdResponse resp = userService.getUserById(id);

        // Assert
        assertNotNull(resp);
        assertEquals(id, resp.getUserID());
        assertEquals("sondeptrai", resp.getUsername());
        assertEquals("Nguyen Van A", resp.getFullName());
        assertEquals("son@fpt.edu.vn", resp.getEmail());
        assertEquals("HE160000", resp.getUserCode());
        assertEquals("0987654321", resp.getPhoneNumber());
        assertEquals(GenderEnum.MALE, resp.getGender());
        assertEquals(RoleEnum.RESIDENT, resp.getRole());
        assertEquals("avatar.png", resp.getImage());

        verify(userRepository, times(1)).findById(id);
    }

    // 🎯 TC11: Lấy user theo id nhưng không tồn tại -> BadHttpRequestException
    @Test
    void getUserById_shouldThrowBadHttpRequest_whenUserNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        BadHttpRequestException ex = assertThrows(
                BadHttpRequestException.class,
                () -> userService.getUserById(id)
        );

        assertEquals("User not found", ex.getMessage());
        verify(userRepository, times(1)).findById(id);
    }
}