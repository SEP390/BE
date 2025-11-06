package com.capstone.capstone.controller;

import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.repository.SemesterRepository;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
// setup database
@Sql(scripts = "classpath:db/HolidayControllerTest.sql")
class HolidayControllerTest {
    @Autowired
    MockMvc mockMvc;

    Gson gson = new Gson();

    Semester semester;

    String token;

    @BeforeEach
    void setUp(@Autowired SemesterRepository semesterRepository) throws Exception {
        semester = semesterRepository.findCurrent();
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
    void createHoliday_Success() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, String> content = new HashMap<>();
        content.put("holidayName", "Holiday Test");
        content.put("startDate", LocalDate.now().format(formatter));
        content.put("endDate", LocalDate.now().format(formatter));
        content.put("semesterId", semester.getId().toString());

        mockMvc.perform(post("/api/holidays")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(content)))
                .andExpect(status().isCreated());
    }

    @Test
    void createHoliday_NullHolidayName() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, String> content = new HashMap<>();
        content.put("holidayName", null);
        content.put("startDate", LocalDate.now().format(formatter));
        content.put("endDate", LocalDate.now().format(formatter));
        content.put("semesterId", semester.getId().toString());

        mockMvc.perform(post("/api/holidays")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(content)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createHoliday_NullStartDate() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, String> content = new HashMap<>();
        content.put("holidayName", "Holiday Test");
        content.put("startDate", null);
        content.put("endDate", LocalDate.now().format(formatter));
        content.put("semesterId", semester.getId().toString());

        mockMvc.perform(post("/api/holidays")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(content)))
                .andExpect(status().isBadRequest());
    }
}