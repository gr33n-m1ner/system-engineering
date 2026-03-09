package org.massage.parlor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.massage.parlor.model.Role;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    
    private static final Integer USER_ID = 1;
    private static final String SECRET = "AMxmyFp3O9I82Zc44ppw9eCW8u75+oIv4u7dENqjyCZRcvo1gRvptjFHkXGkfHUl";
    private static final String INVALID_TOKEN = "invalid.token.here";
    private static final Long EXPIRATION = 86400000L;
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }
    
    @Test
    void shouldGenerateToken() {
        String token = jwtUtil.generateToken(USER_ID, Role.CLIENT);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void shouldExtractUserIdFromToken() {
        String token = jwtUtil.generateToken(USER_ID, Role.CLIENT);
        
        Integer extractedUserId = jwtUtil.extractUserId(token);
        
        assertEquals(USER_ID, extractedUserId);
    }
    
    @Test
    void shouldExtractRoleFromToken() {
        String token = jwtUtil.generateToken(2, Role.ADMIN);
        
        Role extractedRole = jwtUtil.extractRole(token);
        
        assertEquals(Role.ADMIN, extractedRole);
    }
    
    @Test
    void shouldValidateValidToken() {
        String token = jwtUtil.generateToken(USER_ID, Role.CLIENT);
        
        assertTrue(jwtUtil.isTokenValid(token));
    }
    
    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtUtil.isTokenValid(INVALID_TOKEN));
    }
}
