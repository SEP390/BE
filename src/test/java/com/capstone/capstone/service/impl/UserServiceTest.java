package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.response.user.GetAllResidentResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private EmployeeRepository employeeRepository;


    @InjectMocks
    private UserService userService;

    private List<User> mockUsers;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUsers = new java.util.ArrayList<>();

        // User 1: Resident
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("Duongngo");
        mockUser.setFullName("Ngo Tung Duong");
        mockUser.setEmail("duong@example.com");
        mockUser.setDob(LocalDate.of(2002, 1, 21));
        mockUser.setUserCode("HE160231");
        mockUser.setPhoneNumber("123456789");
        mockUser.setGender(GenderEnum.MALE);
        mockUser.setRole(RoleEnum.RESIDENT);
        mockUsers.add(mockUser);

        // User 2: Resident khác
        User resident2 = new User();
        resident2.setId(UUID.randomUUID());
        resident2.setUsername("Resident2");
        resident2.setFullName("Second Resident");
        resident2.setEmail("resident2@example.com");
        resident2.setDob(LocalDate.of(2003, 5, 10));
        resident2.setUserCode("HE160232");
        resident2.setPhoneNumber("987654321");
        resident2.setGender(GenderEnum.FEMALE);
        resident2.setRole(RoleEnum.RESIDENT);
        mockUsers.add(resident2);
        userRepository.save(resident2);

        // User 3: Resident khác
        User guard = new User();
        guard.setId(UUID.randomUUID());
        guard.setUsername("Guard1");
        guard.setFullName("Security Guard");
        guard.setEmail("guard@example.com");
        guard.setDob(LocalDate.of(2000, 8, 15));
        guard.setUserCode("SE000001");
        guard.setPhoneNumber("555555555");
        guard.setGender(GenderEnum.MALE);
        guard.setRole(RoleEnum.RESIDENT);
        mockUsers.add(guard);
        userRepository.save(guard);
    }

    @Test
    void testGetAllResidents_ShouldReturnListOfResidents() {

        when(userRepository.findUserByRole(RoleEnum.RESIDENT)).thenReturn(List.of(mockUser));
        List<GetAllResidentResponse> residents = userService.getAllResidents();

        assertEquals(3, residents.size());
//        assertEquals("Duongngo", mockUser.getUsername());
//        assertEquals("Ngo Tung Duong", mockUser.getFullName());
//        assertEquals("Ngo Tung Duong", mockUser.getEmail());
//        assertEquals(LocalDate.of(2002, 1, 21), mockUser.getDob());
//        assertEquals("HE160231", mockUser.getUserCode());
//        assertEquals("123456789", mockUser.getPhoneNumber());
//        assertEquals(GenderEnum.MALE, mockUser.getGender());
//        assertEquals(RoleEnum.RESIDENT, mockUser.getRole());

        verify(userRepository, times(1)).findUserByRole(RoleEnum.RESIDENT);

    }
}
