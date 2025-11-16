package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.UserRepository;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {
    @Autowired
    MockMvc mvc;
    Gson gson = new Gson();

    /**
     * Prepare User with username "resident" and password "resident"
     */
    @BeforeEach
    void setup(@Autowired UserRepository userRepository, @Autowired PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setUsername("resident");
        user.setRole(RoleEnum.RESIDENT);
        user.setPassword(passwordEncoder.encode("resident"));
        userRepository.save(user);
    }

    /**
     * TC 1: login successfully
     */
    @Test
    void auth_Success() throws Exception {
        Map<String, String> content = new HashMap<>();
        content.put("username", "resident");
        content.put("password", "resident");
        mvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(content)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void auth_NullUsername() throws Exception {
        Map<String, String> content = new HashMap<>();
        content.put("username", null);
        content.put("password", "resident");
        mvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(content)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is required"));
    }

    @Test
    void auth_NullPassword() throws Exception {
        Map<String, String> content = new HashMap<>();
        content.put("username", "resident");
        content.put("password", null);

        mvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(content)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password is required"));
    }

    @Test
    void auth_BadCredentials() throws Exception {
        Map<String, String> content = new HashMap<>();
        content.put("username", "resident");
        content.put("password", "wrong-password");

        mvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(content)))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string("Bad credentials"));
    }
}