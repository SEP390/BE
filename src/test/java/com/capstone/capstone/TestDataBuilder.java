package com.capstone.capstone;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.electricwater.CreateElectricWaterPricingRequest;
import com.capstone.capstone.entity.Dorm;
import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.impl.DormService;
import com.capstone.capstone.service.impl.ElectricWaterService;
import com.capstone.capstone.service.impl.RoomPricingService;
import com.capstone.capstone.service.impl.SemesterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("dev")
class TestDataBuilder {
    @Autowired
    DormService dormService;
    @Autowired
    RoomPricingService roomPricingService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    SemesterService semesterService;
    @Autowired
    private ElectricWaterService electricWaterService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    Random random = new Random();

    @Test
    void generate() {
        generateSemester();
        generateRoomPricing();
        generateDorm();
//        generateUser();
        generateEWPricing();
    }

    void generateSemester() {
        semesterService.create("SP24", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));
        semesterService.create("SU24", LocalDate.of(2024, 5, 1), LocalDate.of(2024, 7, 31));
        semesterService.create("FA24", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 11, 30));
        semesterService.create("SP25", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        semesterService.create("SU25", LocalDate.of(2025, 5, 1), LocalDate.of(2025, 7, 31));
        semesterService.create("FA25", LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));
        semesterService.create("SU26", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
    }

    void generateRoomPricing() {
        roomPricingService.create(6, 800000L);
        roomPricingService.create(4, 1000000L);
        roomPricingService.create(2, 1200000L);
    }

    void generateEWPricing() {
        CreateElectricWaterPricingRequest request = new CreateElectricWaterPricingRequest();
        request.setElectricPrice(3500L);
        request.setWaterPrice(3000L);
        electricWaterService.createPricing(request);
    }

    User createUser(String username, String email, GenderEnum gender, RoleEnum role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setFullName(Arrays.stream(username.split("_")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1)).collect(Collectors.joining(" ")));
        user.setEmail(email);
        user.setDob(LocalDate.of(2025, 1, 1));
        user.setRole(role);
        user.setGender(gender);
        return userRepository.save(user);
    }

    void generateResident() {
        for (int i = 0; i < 3; i++) {
            createUser("resident_male_%s".formatted(i), "resident_male_%s@gmail.com".formatted(i), GenderEnum.MALE, RoleEnum.RESIDENT);
        }
        for (int i = 0; i < 3; i++) {
            createUser("resident_female_%s".formatted(i), "resident_female_%s@gmail.com".formatted(i), GenderEnum.FEMALE, RoleEnum.RESIDENT);
        }
    }

//    void generateEmployee() {
//        var dorms = dormService.getAll();
//        for (Dorm dorm : dorms) {
//            String dormNameNormalize = dorm.getDormName().replace(' ', '_').toLowerCase();
//            var user = createUser("guard_%s".formatted(dormNameNormalize), "manager@gmail.com", GenderEnum.MALE, RoleEnum.GUARD);
//            Employee employee = new Employee();
//            employee.setUser(user);
//            employee.setDorm(dorm);
//            employeeRepository.save(employee);
//        }
//        for (Dorm dorm : dorms) {
//            String dormNameNormalize = dorm.getDormName().replace(' ', '_').toLowerCase();
//            var user = createUser("cleaner_%s".formatted(dormNameNormalize), "manager@gmail.com", GenderEnum.MALE, RoleEnum.CLEANER);
//            Employee employee = new Employee();
//            employee.setUser(user);
//            employee.setDorm(dorm);
//            employeeRepository.save(employee);
//        }
//    }

//    void generateUser() {
//        generateResident();
//        generateEmployee();
//        createUser("manager", "manager@gmail.com", GenderEnum.MALE, RoleEnum.MANAGER);
//        createUser("admin", "admin@gmail.com", GenderEnum.MALE, RoleEnum.ADMIN);
//    }

    void generateRoom(Dorm dorm) {
        for (int floor = 1; floor <= dorm.getTotalFloor(); floor++) {
            for (int roomIndex = 1; roomIndex <= 9; roomIndex++) {
                Room room = new Room();
                String roomNumber = String.valueOf(dorm.getDormName().split(" ")[1]) + floor + "%02d".formatted(roomIndex);
                int totalSlot = random.nextInt(1, 4) * 2;
                room.setRoomNumber(roomNumber);
                room.setTotalSlot(totalSlot);
                room.setFloor(floor);
                dormService.addRoom(dorm, room);
            }
        }
    }

    void generateDorm() {
        for (char c = 'A'; c <= 'F'; c++) {
            String dormName = "Dorm " + c;
            int totalFloor = random.nextInt(3, 5);
            Dorm dorm = dormService.create(dormName, totalFloor);
            generateRoom(dorm);
        }
    }
}
