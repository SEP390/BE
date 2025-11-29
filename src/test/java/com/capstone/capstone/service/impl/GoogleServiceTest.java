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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleServiceTest {
    private final String SAMPLE_CREDENTIAL = "sample_google_jwt_token";
    private final String EMAIL_EXISTING = "existing@gmail.com";
    private final String EMAIL_NEW = "longusername@gmail.com";
    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private GoogleService googleService;
    private GoogleRequest googleRequest;

    @BeforeEach
    void setUp() {
        googleRequest = new GoogleRequest();
        googleRequest.setCredential(SAMPLE_CREDENTIAL);
    }

    @Test
    void auth_Success_ExistingUser() throws GeneralSecurityException, IOException {
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class);
        when(googleIdTokenVerifier.verify(SAMPLE_CREDENTIAL)).thenReturn(mockIdToken);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn(EMAIL_EXISTING);
        User existingUser = new User();
        existingUser.setEmail(EMAIL_EXISTING);
        existingUser.setUsername("existing");
        existingUser.setRole(RoleEnum.RESIDENT);
        when(userRepository.findOne(ArgumentMatchers.<Example<User>>any())).thenReturn(Optional.of(existingUser));
        String expectedJwt = "generated_jwt_token";
        when(jwtService.generateToken(existingUser)).thenReturn(expectedJwt);
        AuthResponse response = googleService.auth(googleRequest);
        assertNotNull(response);
        assertEquals(expectedJwt, response.getToken());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void auth_Success_NewUser() throws GeneralSecurityException, IOException {
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class);
        when(googleIdTokenVerifier.verify(SAMPLE_CREDENTIAL)).thenReturn(mockIdToken);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn(EMAIL_NEW);
        when(userRepository.findOne(ArgumentMatchers.<Example<User>>any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("new_user_token");
        AuthResponse response = googleService.auth(googleRequest);
        assertNotNull(response);
        assertEquals("new_user_token", response.getToken());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(EMAIL_NEW, savedUser.getEmail());
        assertEquals("longusername", savedUser.getUsername());
        assertEquals("LONGUSERNAME", savedUser.getFullName());
        assertEquals(RoleEnum.RESIDENT, savedUser.getRole());
        assertEquals(GenderEnum.MALE, savedUser.getGender());
        assertNotNull(savedUser.getDob());
        assertEquals("USERNAME", savedUser.getUserCode());
    }

    @Test
    void auth_Fail_InvalidCredential() throws GeneralSecurityException, IOException {
        when(googleIdTokenVerifier.verify(SAMPLE_CREDENTIAL)).thenReturn(null);
        AppException exception = assertThrows(AppException.class, () -> googleService.auth(googleRequest));
        assertEquals("INVALID_CREDENTIAL", exception.getMessage());
        verify(userRepository, never()).findOne(ArgumentMatchers.<Specification<User>>any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void auth_Fail_GoogleSecurityException() throws GeneralSecurityException, IOException {
        when(googleIdTokenVerifier.verify(SAMPLE_CREDENTIAL)).thenThrow(new GeneralSecurityException("Security error"));
        AppException exception = assertThrows(AppException.class, () -> googleService.auth(googleRequest));
        assertEquals("INVALID_GOOGLE_TOKEN", exception.getMessage());
    }

    @Test
    void auth_Fail_IOException() throws GeneralSecurityException, IOException {
        when(googleIdTokenVerifier.verify(SAMPLE_CREDENTIAL)).thenThrow(new IOException("Network error"));
        AppException exception = assertThrows(AppException.class, () -> googleService.auth(googleRequest));
        assertEquals("INVALID_GOOGLE_TOKEN", exception.getMessage());
    }
}