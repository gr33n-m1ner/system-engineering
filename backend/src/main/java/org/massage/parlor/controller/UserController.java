package org.massage.parlor.controller;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.UpdateRoleRequest;
import org.massage.parlor.dto.UpdateUserRequest;
import org.massage.parlor.dto.UserResponse;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.User;
import org.massage.parlor.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id, Authentication authentication) {
        Integer currentUserId = Integer.valueOf(authentication.getName());
        
        boolean isAdminOrSpecialist = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                                  auth.getAuthority().equals("ROLE_SPECIALIST"));
        
        if (!isAdminOrSpecialist && !currentUserId.equals(id)) {
            throw new AccessDeniedException("Access denied");
        }
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        return ResponseEntity.ok(toUserResponse(user));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(this::toUserResponse)
                .toList();
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer id,
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        Integer currentUserId = Integer.valueOf(authentication.getName());
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !currentUserId.equals(id)) {
            throw new AccessDeniedException("Access denied");
        }
        
        User updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(toUserResponse(updatedUser));
    }
    
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Integer id,
            @RequestBody UpdateRoleRequest request) {
        User updatedUser = userService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(toUserResponse(updatedUser));
    }
    
    @GetMapping("/login/{login}")
    public ResponseEntity<UserResponse> getUserByLogin(@PathVariable String login, Authentication authentication) {
        Integer currentUserId = Integer.valueOf(authentication.getName());
        User currentUser = userService.getUserById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        boolean isAdminOrSpecialist = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                                  auth.getAuthority().equals("ROLE_SPECIALIST"));
        
        if (!isAdminOrSpecialist && !currentUser.getLogin().equals(login)) {
            throw new AccessDeniedException("Access denied");
        }
        
        User user = userService.getUserByLogin(login)
                .orElseThrow(() -> new NotFoundException("User with login " + login + " not found"));
        return ResponseEntity.ok(toUserResponse(user));
    }
    
    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getLogin(),
                user.getName(),
                user.getRole(),
                user.getPhone(),
                user.getAdditionalInfo(),
                user.getCreatedAt()
        );
    }
}
