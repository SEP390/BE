package com.capstone.capstone.service.impl;
import com.capstone.capstone.dto.request.auth.AuthRequest;
import com.capstone.capstone.dto.response.auth.AuthResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.interfaces.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        User user = (User) authentication.getPrincipal();
        var token = jwtService.generateToken(user);
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setFullName(user.getUsername());
        authResponse.setRole(user.getRole());
        return authResponse;
    }
}
