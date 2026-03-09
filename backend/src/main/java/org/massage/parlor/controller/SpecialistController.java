package org.massage.parlor.controller;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.UpdateSpecialistRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.Specialist;
import org.massage.parlor.service.SpecialistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialists")
@RequiredArgsConstructor
public class SpecialistController {
    
    private final SpecialistService specialistService;
    
    @GetMapping("/{id}")
    public ResponseEntity<Specialist> getSpecialistById(@PathVariable Integer id) {
        Specialist specialist = specialistService.getSpecialistById(id)
                .orElseThrow(() -> new NotFoundException("Specialist with id " + id + " not found"));
        return ResponseEntity.ok(specialist);
    }
    
    @GetMapping
    public ResponseEntity<List<Specialist>> getAllSpecialists() {
        List<Specialist> specialists = specialistService.getAllSpecialists();
        return ResponseEntity.ok(specialists);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Specialist> updateSpecialist(
            @PathVariable Integer id,
            @RequestBody UpdateSpecialistRequest request,
            Authentication authentication) {
        Integer currentUserId = Integer.valueOf(authentication.getName());
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !currentUserId.equals(id)) {
            throw new AccessDeniedException("Access denied");
        }
        
        Specialist updatedSpecialist = specialistService.updateSpecialist(id, request);
        return ResponseEntity.ok(updatedSpecialist);
    }
}
