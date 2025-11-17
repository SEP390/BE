package com.capstone.capstone.controller;

import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.repository.SlotRepository;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@Sql(scripts = "classpath:db/BookingControllerTest.sql")
public class BookingControllerTest {

    @Autowired
    MockMvc mockMvc;
    Gson gson = new Gson();

    String token;

    @BeforeEach
    void setUp() throws Exception {
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
    void createBooking(@Autowired SlotRepository slotRepository) throws Exception {
        Slot slot = slotRepository.findAll((r, q, c) -> {
            return c.isNull(r.get("user"));
        }).getFirst();
        mockMvc.perform(post("/api/booking")
                        .header("Authorization", "Bearer " + token)
                        .param("slotId", slot.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty());
    }
}
