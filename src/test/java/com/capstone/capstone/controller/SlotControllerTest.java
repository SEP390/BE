package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.repository.UserRepository;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class SlotControllerTest {

    @Autowired
    SlotRepository slotRepository;
    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MockMvc mockMvc;
    Gson gson = new Gson();

    String token;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    User user;
    Room room;
    Slot slot;

    @BeforeEach
    void setup() throws Exception {
        user = new User();
        user.setUsername("test");
        user.setRole(RoleEnum.RESIDENT);
        user.setPassword(passwordEncoder.encode("test"));
        user = userRepository.save(user);
        room = new Room();
        room.setRoomNumber("Room Test");
        room.setTotalSlot(1);
        room.setFloor(1);
        room = roomRepository.save(room);
        slot = new Slot();
        slot.setSlotName("Slot Test");
        slot.setStatus(StatusSlotEnum.CHECKIN);
        slot.setRoom(room);
        slot.setUser(user);
        slotRepository.save(slot);

        // api login
        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(Map.of(
                                "username", "test",
                                "password", "test"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andDo(result -> {
                    token = JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
                });
    }

    @Test
    void getAllCheckin() throws Exception {
        mockMvc.perform(get("/api/slots/checkin").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)))
                .andDo(print());
    }

    @Test
    void checkin() throws Exception {
        mockMvc.perform(post("/api/slots/checkin/" + slot.getId().toString()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
        assertThat(slotRepository.findById(slot.getId()).orElseThrow().getStatus()).isEqualTo(StatusSlotEnum.UNAVAILABLE);
    }
}