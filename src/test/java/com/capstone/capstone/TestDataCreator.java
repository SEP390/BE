package com.capstone.capstone;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

@SpringBootTest
@ActiveProfiles("dev")
public class TestDataCreator {
    @Autowired
    private RoomPricingRepository roomPricingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SlotRepository slotRepository;
    @Autowired
    private DormRepository dormRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private SemesterRepository semesterRepository;
    @Autowired
    private SurveyQuestionRepository surveyQuestionRepository;
    @Autowired
    private SurveyOptionRepository surveyOptionRepository;
    @Autowired
    private SurveySelectRepository surveySelectRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int USER_COUNT = 10;
    private static final String USER_DEFAULT_PASSWORD = "resident";
    private static final GenderEnum USER_DEFAULT_GENDER = GenderEnum.MALE;
    private static final Date USER_DEFAULT_DOB = Date.from(new GregorianCalendar(2025, Calendar.JANUARY, 1).toInstant());
    private static final int FLOOR_PER_DORM = 4;
    private static final int ROOM_PER_FLOOR = 10;
    private static final int ROOM_PER_DORM = ROOM_PER_FLOOR * FLOOR_PER_DORM;

    private final Random random = new Random();
    @Autowired
    private SlotHistoryRepository slotHistoryRepository;

    private String generateRandomString() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    @Test
    public void generateRoomPricing() {
        var roomPricing = List.of(RoomPricing.builder().price(1200000).totalSlot(2).build(), RoomPricing.builder().price(1000000).totalSlot(4).build(), RoomPricing.builder().price(800000).totalSlot(6).build());
        roomPricingRepository.saveAll(roomPricing);
    }

    private User createUser(User user) {
        if (user.getUsername() == null) {
            user.setUsername(generateRandomString());
        }
        if (user.getEmail() == null) {
            var email = "%s@gmail.com".formatted(user.getUsername());
            user.setEmail(email);
        }
        if (user.getPassword() == null) {
            user.setPassword(passwordEncoder.encode(USER_DEFAULT_PASSWORD));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getDob() == null) {
            user.setDob(USER_DEFAULT_DOB);
        }
        if (user.getGender() == null) {
            user.setGender(USER_DEFAULT_GENDER);
        }
        if (user.getRole() == null) {
            user.setRole(RoleEnum.RESIDENT);
        }
        return user;
    }

    @Test
    public void generateUsers() {
        var users = new ArrayList<User>();

        users.add(createUser(User.builder().username("resident").build()));
        users.add(createUser(User.builder().username("admin").role(RoleEnum.ADMIN).build()));
        for(var i = 0; i < USER_COUNT; i++) {
            User user = new User();
            users.add(createUser(user));
        }
        userRepository.saveAll(users);
    }

    @Test
    public void generateUserSlot() {
        var users = userRepository.findAll();
        var slots = slotRepository.findAll();
        for (User user : users) {
            var slot = slots.get(random.nextInt(slots.size()));
            slot.setUser(user);
            slot.setStatus(StatusSlotEnum.UNAVAILABLE);
            slotRepository.save(slot);
        }
    }

    @Test
    public void generateSlot() {
        var rooms = roomRepository.findAll();
        var slots = new ArrayList<Slot>();
        for (var room : rooms) {
            for (var i = 1; i <= room.getTotalSlot(); i++) {
                Slot slot = new Slot();
                slot.setSlotName("Slot %s".formatted(i));
                slot.setRoom(room);
                slot.setStatus(StatusSlotEnum.AVAILABLE);
                slots.add(slot);
            }
        }
        slotRepository.saveAll(slots);
    }

    @Test
    public void generateDorm() {
        var dorms = new ArrayList<Dorm>();
        for (var i = 'A'; i <= 'F'; i++) {
            Dorm dorm = new Dorm();
            dorm.setTotalRoom(ROOM_PER_DORM);
            dorm.setTotalFloor(FLOOR_PER_DORM);
            dorm.setDormName("Dorm %s".formatted(i));
            dorms.add(dorm);
        }
        dormRepository.saveAll(dorms);
    }

    @Test
    public void generateRoom() {
        var dorms = dormRepository.findAll();
        var pricing = roomPricingRepository.findAll();

        var rooms = new ArrayList<Room>();
        for (Dorm dorm : dorms) {
            for (int floor = 1; floor <= dorm.getTotalFloor(); floor++) {
                for (int r = 0; r < ROOM_PER_FLOOR; r++) {
                    var dormLabel = dorm.getDormName().split(" ")[1];
                    Room room = new Room();
                    room.setDorm(dorm);
                    room.setFloor(floor);
                    room.setRoomNumber("%s%s%02d".formatted(dormLabel, floor, r));
                    room.setStatus(StatusRoomEnum.AVAILABLE);
                    var randomPricing = pricing.get(random.nextInt(pricing.size()));
                    room.setTotalSlot(randomPricing.getTotalSlot());
                    rooms.add(room);
                }
            }
        }
        roomRepository.saveAll(rooms);
    }

    @Test
    public void generateAll() {
        deleteAllData();
        generateSemester();
        generateUsers();
        generateDorm();
        generateRoomPricing();
        generateRoom();
        generateSlot();
        // generateUserSlot();
        generateSurveyData();
    }

    @Test
    public void generateSemester() {
        // prev sem
        Semester prev = new Semester();
        prev.setName("SU25");
        prev.setStartDate(LocalDate.of(2025, 5, 15));
        prev.setEndDate(LocalDate.of(2025, 8, 15));
        // next sem
        Semester next = new Semester();
        next.setName("FA25");
        next.setStartDate(LocalDate.of(2025, 10, 15));
        next.setEndDate(LocalDate.of(2025, 12, 15));
        semesterRepository.save(next);
        semesterRepository.save(prev);
    }

    @Test
    public void generateSurveyData() {
        final List<SurveyQuestion> questions = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            SurveyQuestion q = new SurveyQuestion();
            q.setQuestionContent(generateRandomString());
            questions.add(surveyQuestionRepository.save(q));
        }
        questions.forEach(surveyQuestion -> {
            final List<SurveyOption> options = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                SurveyOption o = new SurveyOption();
                o.setSurveyQuestion(surveyQuestion);
                o.setOptionContent(generateRandomString());
                options.add(surveyOptionRepository.save(o));
            }
            surveyQuestion.setSurveyOptions(options);
        });
        final List<User> users = userRepository.findAll();
        users.forEach(user -> {
            questions.forEach(surveyQuestion -> {
                SurveyOption o = surveyQuestion.getSurveyOptions().get(random.nextInt(surveyQuestion.getSurveyOptions().size()));
                SurveyQuetionSelected s = new SurveyQuetionSelected();
                s.setSurveyOption(o);
                s.setUser(user);
                surveySelectRepository.save(s);
            });
        });
    }

    @Test
    void deleteAllData() {
        slotHistoryRepository.deleteAll();
        slotRepository.deleteAll();
        roomRepository.deleteAll();
        dormRepository.deleteAll();
        roomPricingRepository.deleteAll();
        semesterRepository.deleteAll();
        surveySelectRepository.deleteAll();
        surveyOptionRepository.deleteAll();
        surveyQuestionRepository.deleteAll();
        userRepository.deleteAll();
    }
}
