package org.massage.parlor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.massage.parlor.dto.UpdateUserRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.Role;
import org.massage.parlor.model.Specialist;
import org.massage.parlor.model.User;
import org.massage.parlor.repository.SpecialistRepository;
import org.massage.parlor.repository.UserRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    private static final String LOGIN = "testuser";
    private static final String NAME = "Test Name";
    private static final String PASSWORD_HASH = "passwordHash";
    private static final Integer USER_ID_1 = 1;
    private static final Integer USER_ID_2 = 2;
    private static final Integer NONEXISTENT_ID = 999;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SpecialistRepository specialistRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldUpdateUser() {
        User existingUser = new User();
        existingUser.setId(USER_ID_1);
        existingUser.setLogin(LOGIN);
        existingUser.setName("Old Name");
        existingUser.setRole(Role.CLIENT);
        
        UpdateUserRequest request = new UpdateUserRequest("New Name", "newpassword", "+9876543210", "New info");
        
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        User updatedUser = userService.updateUser(USER_ID_1, request);
        
        assertEquals(LOGIN, updatedUser.getLogin());
        assertEquals("New Name", updatedUser.getName());
        assertEquals("encodedNewPassword", updatedUser.getPasswordHash());
        assertEquals(Role.CLIENT, updatedUser.getRole());
        assertEquals("+9876543210", updatedUser.getPhone());
        assertEquals("New info", updatedUser.getAdditionalInfo());
        
        verify(userRepository).save(existingUser);
    }
    
    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentUser() {
        UpdateUserRequest request = new UpdateUserRequest(NAME, null, null, null);
        
        when(userRepository.findById(NONEXISTENT_ID)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> userService.updateUser(NONEXISTENT_ID, request));
    }
    
    @Test
    void shouldUpdateUserRole() {
        User existingUser = new User();
        existingUser.setId(USER_ID_1);
        existingUser.setLogin(LOGIN);
        existingUser.setRole(Role.CLIENT);
        
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        User updatedUser = userService.updateUserRole(USER_ID_1, Role.ADMIN);
        
        assertEquals(Role.ADMIN, updatedUser.getRole());
        verify(userRepository).save(existingUser);
        verify(specialistRepository, never()).save(any());
    }
    
    @Test
    void shouldCreateSpecialistRecordWhenChangingRoleToSpecialist() {
        User existingUser = new User();
        existingUser.setId(USER_ID_1);
        existingUser.setLogin(LOGIN);
        existingUser.setRole(Role.CLIENT);
        
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(specialistRepository.findById(USER_ID_1)).thenReturn(Optional.empty());
        
        User updatedUser = userService.updateUserRole(USER_ID_1, Role.SPECIALIST);
        
        assertEquals(Role.SPECIALIST, updatedUser.getRole());
        verify(userRepository).save(existingUser);
        verify(specialistRepository).save(any(Specialist.class));
    }
    
    @Test
    void shouldDeactivateSpecialistRecordWhenChangingRoleFromSpecialist() {
        User existingUser = new User();
        existingUser.setId(USER_ID_1);
        existingUser.setLogin(LOGIN);
        existingUser.setRole(Role.SPECIALIST);
        
        Specialist specialist = new Specialist();
        specialist.setUser(existingUser);
        specialist.setActive(true);
        
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(specialistRepository.findById(USER_ID_1)).thenReturn(Optional.of(specialist));
        
        User updatedUser = userService.updateUserRole(USER_ID_1, Role.CLIENT);
        
        assertEquals(Role.CLIENT, updatedUser.getRole());
        assertFalse(specialist.getActive());
        verify(userRepository).save(existingUser);
        verify(specialistRepository).save(specialist);
    }
    
    @Test
    void shouldReactivateSpecialistRecordWhenChangingRoleToSpecialist() {
        User existingUser = new User();
        existingUser.setId(USER_ID_1);
        existingUser.setLogin(LOGIN);
        existingUser.setRole(Role.CLIENT);
        
        Specialist specialist = new Specialist();
        specialist.setUser(existingUser);
        specialist.setActive(false);
        
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(specialistRepository.findById(USER_ID_1)).thenReturn(Optional.of(specialist));
        
        User updatedUser = userService.updateUserRole(USER_ID_1, Role.SPECIALIST);
        
        assertEquals(Role.SPECIALIST, updatedUser.getRole());
        assertTrue(specialist.getActive());
        verify(userRepository).save(existingUser);
        verify(specialistRepository).save(specialist);
    }
    
    @Test
    void shouldThrowExceptionWhenUpdatingRoleOfNonexistentUser() {
        when(userRepository.findById(NONEXISTENT_ID)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> userService.updateUserRole(NONEXISTENT_ID, Role.ADMIN));
    }
    
}
