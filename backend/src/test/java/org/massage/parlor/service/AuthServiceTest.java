package org.massage.parlor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.massage.parlor.dto.AuthResponse;
import org.massage.parlor.dto.LoginRequest;
import org.massage.parlor.dto.RegisterRequest;
import org.massage.parlor.model.Role;
import org.massage.parlor.model.User;
import org.massage.parlor.repository.UserRepository;
import org.massage.parlor.security.JwtUtil;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String LOGIN = "testuser";
    private static final String PASSWORD = "password123";
    private static final String WRONG_PASSWORD = "wrongpassword";
    private static final String HASHED_PASSWORD = "hashedPassword";
    private static final String PHONE = "+1234567890";
    private static final String JWT_TOKEN = "test.jwt.token";
    private static final String NAME = "Test User";
    private static final Integer USER_ID = 1;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest(LOGIN, PASSWORD, NAME, PHONE);
        User savedUser = new User();
        savedUser.setId(USER_ID);
        savedUser.setLogin(LOGIN);
        savedUser.setRole(Role.CLIENT);
        
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(USER_ID, Role.CLIENT)).thenReturn(JWT_TOKEN);
        
        AuthResponse response = authService.register(request);
        
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        assertEquals(LOGIN, response.getLogin());
        assertEquals(Role.CLIENT, response.getRole());
        
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void shouldThrowExceptionWhenRegisteringExistingLogin() {
        RegisterRequest request = new RegisterRequest("existing", PASSWORD, NAME, null);
        
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException(""));
        
        assertThrows(DataIntegrityViolationException.class, () -> authService.register(request));
    }
    
    @Test
    void shouldThrowExceptionWhenRegisteringExistingPhone() {
        RegisterRequest request = new RegisterRequest(LOGIN, PASSWORD, NAME, PHONE);
        
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException(""));
        
        assertThrows(DataIntegrityViolationException.class, () -> authService.register(request));
    }
    
    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest(LOGIN, PASSWORD);
        User user = new User();
        user.setId(USER_ID);
        user.setLogin(LOGIN);
        user.setPasswordHash(HASHED_PASSWORD);
        user.setRole(Role.CLIENT);
        
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(jwtUtil.generateToken(USER_ID, Role.CLIENT)).thenReturn(JWT_TOKEN);
        
        AuthResponse response = authService.login(request);
        
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        assertEquals(LOGIN, response.getLogin());
        assertEquals(Role.CLIENT, response.getRole());
    }
    
    @Test
    void shouldThrowExceptionWhenLoginWithInvalidPassword() {
        LoginRequest request = new LoginRequest(LOGIN, WRONG_PASSWORD);
        User user = new User();
        user.setPasswordHash(HASHED_PASSWORD);
        
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, HASHED_PASSWORD)).thenReturn(false);
        
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
    
    @Test
    void shouldThrowExceptionWhenLoginWithNonexistentUser() {
        LoginRequest request = new LoginRequest("nonexistent", PASSWORD);
        
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());
        
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
