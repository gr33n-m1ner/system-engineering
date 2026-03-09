package org.massage.parlor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.massage.parlor.dto.AuthResponse;
import org.massage.parlor.dto.LoginRequest;
import org.massage.parlor.dto.RegisterRequest;
import org.massage.parlor.model.Role;
import org.massage.parlor.security.JwtAuthenticationFilter;
import org.massage.parlor.security.JwtUtil;
import org.massage.parlor.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    
    private static final String LOGIN = "testuser";
    private static final String PASSWORD = "password123";
    private static final String JWT_TOKEN = "test.jwt.token";
    private static final String NAME = "Test User";
    private static final String ROLE_CLIENT = "CLIENT";
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Test
    void shouldRegisterNewUser() throws Exception {
        RegisterRequest request = new RegisterRequest(LOGIN, PASSWORD, NAME, "+1234567890");
        AuthResponse response = new AuthResponse(JWT_TOKEN, LOGIN, Role.CLIENT);
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(JWT_TOKEN))
                .andExpect(jsonPath("$.login").value(LOGIN))
                .andExpect(jsonPath("$.role").value(ROLE_CLIENT));
    }
    
    @Test
    void shouldReturnBadRequestWhenRegisteringExistingUser() throws Exception {
        RegisterRequest request = new RegisterRequest("existing", PASSWORD, NAME, null);
        
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DataIntegrityViolationException(""));
        
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest(LOGIN, PASSWORD);
        AuthResponse response = new AuthResponse(JWT_TOKEN, LOGIN, Role.CLIENT);
        
        when(authService.login(any(LoginRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(JWT_TOKEN))
                .andExpect(jsonPath("$.login").value(LOGIN))
                .andExpect(jsonPath("$.role").value(ROLE_CLIENT));
    }
    
    @Test
    void shouldReturnUnauthorizedWhenLoginWithInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest(LOGIN, "wrongpassword");
        
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
