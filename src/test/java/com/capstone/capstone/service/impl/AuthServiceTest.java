package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.auth.AuthRequest;
import com.capstone.capstone.dto.response.auth.AuthResponse;
import com.capstone.capstone.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test without mocked database result
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    JwtService jwtService;
    @Mock
    AuthenticationManager authenticationManager;
    @InjectMocks
    AuthService authService;

    @Mock
    Authentication authentication;

    @Test
    void login_Success() {
        User user = new User();
        user.setUsername("resident");
        user.setRole(RoleEnum.RESIDENT);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("token");

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("resident");
        authRequest.setPassword("resident");
        AuthResponse response = authService.login(authRequest);

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(RoleEnum.RESIDENT);
        assertThat(response.getToken()).isEqualTo("token");
    }

    @Test
    void login_BadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(BadCredentialsException.class);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("resident");
        authRequest.setPassword("resident");

        assertThatThrownBy(() -> authService.login(authRequest)).isInstanceOf(BadCredentialsException.class);
    }
}
