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
    private PasswordEncoder passwordEncoder;

    private static final int USER_COUNT = 10;
    private static final String USER_DEFAULT_PASSWORD = "resident";
    private static final Date USER_DEFAULT_DOB = Date.from(new GregorianCalendar(2025, Calendar.JANUARY, 1).toInstant());
    private static final int FLOOR_PER_DORM = 4;
    private static final int ROOM_PER_FLOOR = 10;
    private static final int ROOM_PER_DORM = ROOM_PER_FLOOR * FLOOR_PER_DORM;

    private final Random random = new Random();

    private String generateRandomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Test
    public void generateRoomPricing() {
        var roomPricing = List.of(RoomPricing.builder().price(1200000).totalSlot(2).build(), RoomPricing.builder().price(1000000).totalSlot(4).build(), RoomPricing.builder().price(800000).totalSlot(6).build());
        roomPricingRepository.saveAll(roomPricing);
    }

    private User createUser(String username, String email, String password, RoleEnum role, GenderEnum gender, Date dob) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setDob(dob);
        user.setGender(gender);
        user.setRole(role);
        return user;
    }

    @Test
    public void generateUser() {
        var username = "hieulm";
        var email = "hieulmhe17623@fpt.edu.vn";
        var password = "hieulm";
        var role = RoleEnum.ADMIN;
        var gender = GenderEnum.MALE;
        var dob = USER_DEFAULT_DOB;
        userRepository.save(createUser(username, email, password, role, gender, dob));
    }

    @Test
    public void generateUsers() {
        var users = new ArrayList<User>();
        for(var i = 0; i < USER_COUNT; i++) {
            var username = generateRandomString();
            var email = "%s@gmail.com".formatted(generateRandomString());
            var password = USER_DEFAULT_PASSWORD;
            var gender = random.nextInt(2) == 0 ? GenderEnum.MALE : GenderEnum.FEMALE;
            var dob = USER_DEFAULT_DOB;
            var role = RoleEnum.RESIDENT;
            users.add(createUser(username, email, password, role, gender, dob));
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
        generateUsers();

    }
}
