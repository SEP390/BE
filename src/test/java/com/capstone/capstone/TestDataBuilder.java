package com.capstone.capstone;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
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

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

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
    @Autowired
    private SlotRepository slotRepository;
    @Autowired
    private SemesterRepository semesterRepository;
    @Autowired
    private SlotHistoryRepository slotHistoryRepository;

    @Test
    void generate() {
        generateSemester();
        generateRoomPricing();
        generateDorm();
    }

    @Test
    void generateSemester() {
        semesterService.create("SP24", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 30));
        semesterService.create("SU24", LocalDate.of(2024, 5, 1), LocalDate.of(2024, 8, 31));
        semesterService.create("FA24", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 12, 31));
        semesterService.create("SP25", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 4, 30));
        semesterService.create("SU25", LocalDate.of(2025, 5, 1), LocalDate.of(2025, 8, 31));
        semesterService.create("FA25", LocalDate.of(2025, 9, 1), LocalDate.of(2025, 12, 31));
        semesterService.create("SP26", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 30));
        semesterService.create("SU26", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 31));
        semesterService.create("FA26", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 31));
    }

    @Test
    void generateRoomPricing() {
        roomPricingService.create(6, 800_000L);
        roomPricingService.create(4, 1_000_000L);
        roomPricingService.create(2, 1_200_000L);
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
        String[] questionContent = new String[]{
                "Bạn có thường xuyên sử dụng không gian chung (bếp, phòng sinh hoạt, khu tự học) không?",
                "Bạn có sẵn lòng chia sẻ đồ dùng chung với bạn bè cùng tầng/khu vực không?",
                "Bạn có tập thể dục thể thao không?",
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
        List<User> users = userRepository.findAll((r,q,c) -> {
            return c.equal(r.get("role"), RoleEnum.RESIDENT);
        });
        users.forEach(user -> {
            surveyQuestions.forEach(question -> {
                var opts = surveyOptionRepository.findAll((r, q, c) -> {
                    return c.equal(r.get("surveyQuestion"), question);
                });
                SurveyQuetionSelected selected = new SurveyQuetionSelected();
                selected.setUser(user);
                selected.setSurveyOption(opts.get(random.nextInt(opts.size())));
                surveySelectRepository.save(selected);
            });
        });
    }

    private String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        return temp.replaceAll("đ", "d").replaceAll("Đ", "D");
    }

    private String normalize(String fullName) {
        String[] split = fullName.split(" ");
        return removeAccent(split[2].toLowerCase()) + split[0].substring(0, 1).toLowerCase() + split[1].substring(0, 1).toLowerCase();
    }

    @Test
    void generateUserSlot() {
        List<User> users = userRepository.findAll((r,q,c) -> {
            return c.equal(r.get("role"), RoleEnum.RESIDENT);
        });
        Semester semester = semesterRepository.findNextSemester();
        List<Slot> slots = slotRepository.findAll();
        Collections.shuffle(slots);
        slots = slots.subList(0, users.size());
        List<SlotHistory> needSaved = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            var slot = slots.get(i);
            var user = users.get(i);
            slot.setUser(user);
            slot.setStatus(StatusSlotEnum.UNAVAILABLE);
            SlotHistory slotHistory = new SlotHistory();
            slotHistory.setSlotId(slot.getId());
            slotHistory.setUser(user);
            slotHistory.setSemester(semester);
            slotHistory.setSlotName(slot.getSlotName());
            slotHistory.setCheckin(LocalDateTime.now());
            slotHistory.setRoom(slot.getRoom());
            needSaved.add(slotHistory);
        }
        slotRepository.saveAll(slots);
        slotHistoryRepository.saveAll(needSaved);
    }


    @Test
    void generateUser() {
        String[] names = new String[]{
                "Mai Kiến Bình",
                "Cống Thế Bình",
                "Hoàng Việt Khang",
                "Bồ Quốc Hiền",
                "Vạn Gia Lập",
                "Hàn Trọng Nghĩa",
                "Lâm Trung Nhân",
                "Sử Trung Nhân",
                "Đống Minh Quý",
                "Hán Ðức Siêu",
                "Ngân Cao Tiến",
                "Hàng Mạnh Trường",
                "Quách Hoàng Thái",
                "Tri Quang Thái",
                "Sầm Huy Việt",
                "Uông Quý Vĩnh",
        };
        List<Integer> codePool = Arrays.stream(names).map(item -> random.nextInt(100000, 999999)).toList();
        int code = 123456;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            var name = names[i];
            User user = new User();
            user.setFullName(name);
            String normalized = normalize(name);
            String username = normalized + "he" + code;
            user.setUsername(username);
            String email = "%s@fpt.edu.vn".formatted(username);
            String userCode = "HE" + codePool.get(i);
            user.setUserCode(userCode);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setGender(GenderEnum.MALE);
            user.setRole(RoleEnum.RESIDENT);
            user.setDob(LocalDate.now());
            user.setPhoneNumber("0912345678");
            users.add(user);
        }
        userRepository.saveAll(users);
    }

    @Test
    void generateGuard() {
        User user = new User();
        user.setRole(RoleEnum.GUARD);
        user.setUsername("guard");
        user.setPassword(passwordEncoder.encode("123456"));
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
