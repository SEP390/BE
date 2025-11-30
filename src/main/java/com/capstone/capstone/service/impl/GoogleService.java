package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.auth.GoogleRequest;
import com.capstone.capstone.dto.response.auth.AuthResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor
public class GoogleService {
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthResponse auth(GoogleRequest request) {
        log.info("Request: {}", request);
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(request.getCredential());
            if (idToken == null) {
                throw new AppException("INVALID_CREDENTIAL");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            User userExample = new User();
            userExample.setEmail(email);
            User existed = userRepository.findOne(Example.of(userExample)).orElse(null);
            // create new user if not exist
            if (existed == null) {
                existed = new User();
                existed.setEmail(email);
                existed.setImage(pictureUrl);
                var username = email.split("@")[0];
                var userCode = username.substring(username.length() - 8).toUpperCase();
                existed.setUsername(username);
                existed.setUserCode(userCode);
                existed.setFullName(fullName);
                existed.setDob(LocalDate.now());
                existed.setRole(RoleEnum.RESIDENT);
                existed.setGender(GenderEnum.MALE); // TODO: edit later
                existed = userRepository.save(existed);
            }
            var jwtToken = jwtService.generateToken(existed);
            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(jwtToken);
            return authResponse;
        } catch (GeneralSecurityException | IOException e) {
            log.error(e.getMessage(), e);
            throw new AppException("INVALID_GOOGLE_TOKEN");
        }
    }
}
