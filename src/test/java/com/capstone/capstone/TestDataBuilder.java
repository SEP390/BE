package com.capstone.capstone;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.service.impl.DormService;
import com.capstone.capstone.service.impl.RoomPricingService;
import com.capstone.capstone.service.impl.SemesterService;
import com.capstone.capstone.service.impl.SurveySelectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@SpringBootTest
@ActiveProfiles("dev")
class TestDataBuilder {
    @Autowired
    DormService dormService;
    @Autowired
    RoomPricingService roomPricingService;
    @Autowired
    SemesterService semesterService;
    @Autowired
    SurveyQuestionRepository surveyQuestionRepository;
    @Autowired
    SurveyOptionRepository surveyOptionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SurveySelectService surveySelectService;
    @Autowired
    SurveySelectRepository surveySelectRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    Random random = new Random();
    private EmployeeRepository employeeRepository;
    private DormRepository dormRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    void generate() {
        generateSemester();
        generateRoomPricing();
        generateDorm();
    }

    @Test
    void generateSemester() {
        semesterService.create("SP24", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));
        semesterService.create("SU24", LocalDate.of(2024, 5, 1), LocalDate.of(2024, 7, 31));
        semesterService.create("FA24", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 11, 30));
        semesterService.create("SP25", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        semesterService.create("SU25", LocalDate.of(2025, 5, 1), LocalDate.of(2025, 7, 31));
        semesterService.create("FA25", LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));
        semesterService.create("SP26", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
        semesterService.create("SU26", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 7, 31));
    }

    @Test
    void generateRoomPricing() {
        roomPricingService.create(6, 800000L);
        roomPricingService.create(4, 1000000L);
        roomPricingService.create(2, 1200000L);
    }

    private void generateRoom(Dorm dorm) {
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

    @Test
    void generateDorm() {
        for (char c = 'A'; c <= 'F'; c++) {
            String dormName = "Dorm " + c;
            int totalFloor = random.nextInt(3, 5);
            Dorm dorm = dormService.create(dormName, totalFloor);
            generateRoom(dorm);
        }
    }

    @Test
    void generateSurvey() {
        String[] questionContent = new String[] {
                "Bạn có ngủ trước 11 giờ không?",
                "Bạn có chơi game không?",
                "Bạn có đọc tiểu thuyết không?",
        };
        for (String content : questionContent) {
            SurveyQuestion question = new SurveyQuestion();
            question.setQuestionContent(content);
            question = surveyQuestionRepository.save(question);
            SurveyOption opt1 = new SurveyOption();
            opt1.setSurveyQuestion(question);
            opt1.setOptionContent("Có");
            SurveyOption opt2 = new SurveyOption();
            opt2.setSurveyQuestion(question);
            opt2.setOptionContent("Không");
            surveyOptionRepository.save(opt1);
            surveyOptionRepository.save(opt2);
        }
    }

    @Test
    void generateSurveySelect() {
        List<SurveyQuestion> surveyQuestions = surveyQuestionRepository.findAll();
        List<User> users = userRepository.findAll();
        users.forEach(user -> {
            surveyQuestions.forEach(question -> {
                var opts = surveyOptionRepository.findAll((r,q,c) -> {
                    return c.equal(r.get("surveyQuestion"), question);
                });
                SurveyQuetionSelected selected = new  SurveyQuetionSelected();
                selected.setUser(user);
                selected.setSurveyOption(opts.get(random.nextInt(opts.size())));
                surveySelectRepository.save(selected);
            });
        });
    }

    @Test
    void generateUser() {
        User user = new User();
        user.setRole(RoleEnum.RESIDENT);
        user.setUsername("resident");
        user.setPassword(passwordEncoder.encode("resident"));
        user.setGender(GenderEnum.MALE);
        user.setFullName("Resident");
        user.setUserCode("HE123456");
        user.setDob(LocalDate.now());
        user.setPhoneNumber("0912345678");
        user.setEmail("resident@gmail.com");
        userRepository.save(user);
    }

    @Test
    void generateGuard() {
        User user = new User();
        user.setRole(RoleEnum.GUARD);
        user.setUsername("guard");
        user.setPassword(passwordEncoder.encode("guard"));
        user.setGender(GenderEnum.MALE);
        user.setFullName("Guard");
        user.setUserCode("HE223456");
        user.setDob(LocalDate.now());
        user.setPhoneNumber("0922345678");
        user.setEmail("guard@gmail.com");
        user = userRepository.save(user);
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setHireDate(LocalDate.now());
        employee.setContractEndDate(LocalDate.now().plusDays(30));
        employee = employeeRepository.save(employee);
        Schedule schedule = new Schedule();
        schedule.setEmployee(employee);
        schedule.setDorm(dormRepository.findAll().getFirst());
        scheduleRepository.save(schedule);
    }
}
