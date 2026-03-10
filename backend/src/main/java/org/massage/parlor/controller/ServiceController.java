package org.massage.parlor.controller;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.AddSpecialistOfferingRequest;
import org.massage.parlor.dto.CreateServiceCatalogRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.ServiceCatalog;
import org.massage.parlor.model.SpecialistOffering;
import org.massage.parlor.service.ServiceCatalogService;
import org.massage.parlor.service.SpecialistOfferingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {
    
    private final ServiceCatalogService serviceCatalogService;
    private final SpecialistOfferingService specialistOfferingService;
    
    @GetMapping("/types")
    public ResponseEntity<List<ServiceCatalog>> getAllServiceCatalogs() {
        List<ServiceCatalog> serviceCatalogs = serviceCatalogService.getAllServiceCatalogs();
        return ResponseEntity.ok(serviceCatalogs);
    }
    
    @GetMapping("/types/{id}")
    public ResponseEntity<ServiceCatalog> getServiceCatalogById(@PathVariable Integer id) {
        ServiceCatalog serviceCatalog = serviceCatalogService.getServiceCatalogById(id)
                .orElseThrow(() -> new NotFoundException("Service catalog with id " + id + " not found"));
        return ResponseEntity.ok(serviceCatalog);
    }
    
    @PostMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCatalog> createServiceCatalog(@RequestBody CreateServiceCatalogRequest request) {
        ServiceCatalog serviceCatalog = serviceCatalogService.createServiceCatalog(request);
        return ResponseEntity.ok(serviceCatalog);
    }
    
    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateServiceCatalog(@PathVariable Integer id) {
        serviceCatalogService.deactivateServiceCatalog(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/specialists/{specialistId}")
    public ResponseEntity<List<SpecialistOffering>> getSpecialistOfferings(@PathVariable Integer specialistId) {
        List<SpecialistOffering> offerings = specialistOfferingService.getOfferingsBySpecialistId(specialistId);
        return ResponseEntity.ok(offerings);
    }
    
    @PostMapping("/specialists/{specialistId}")
    public ResponseEntity<SpecialistOffering> addOfferingToSpecialist(
            @PathVariable Integer specialistId,
            @RequestBody AddSpecialistOfferingRequest request,
            Authentication authentication) {
        Integer currentUserId = Integer.valueOf(authentication.getName());
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !currentUserId.equals(specialistId)) {
            throw new AccessDeniedException("Access denied");
        }
        
        SpecialistOffering specialistOffering = specialistOfferingService.addOfferingToSpecialist(specialistId, request);
        return ResponseEntity.ok(specialistOffering);
    }
    
    @DeleteMapping("/specialist-services/{id}")
    public ResponseEntity<Void> deactivateSpecialistOffering(
            @PathVariable Integer id,
            Authentication authentication) {
        Integer currentUserId = Integer.valueOf(authentication.getName());
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        SpecialistOffering existingOffering = specialistOfferingService.getSpecialistOfferingById(id)
                .orElseThrow(() -> new NotFoundException("Specialist offering with id " + id + " not found"));
        
        if (!isAdmin && !currentUserId.equals(existingOffering.getSpecialist().getId())) {
            throw new AccessDeniedException("Access denied");
        }
        
        specialistOfferingService.deactivateSpecialistOffering(id);
        return ResponseEntity.ok().build();
    }
}

