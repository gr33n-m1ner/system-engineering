package org.massage.parlor.controller;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.AppointmentResponse;
import org.massage.parlor.dto.CreateAppointmentRequest;
import org.massage.parlor.dto.UpdateAppointmentStatusRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.Appointment;
import org.massage.parlor.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestBody CreateAppointmentRequest request,
            Authentication authentication) {
        Integer currentUserId = Integer.valueOf(authentication.getName());
        
        Appointment appointment = appointmentService.createAppointment(currentUserId, request);
        return ResponseEntity.ok(toResponse(appointment));
    }
    
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAppointments(Authentication authentication) {
        Integer currentUserId = Integer.valueOf(authentication.getName());
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isSpecialist = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SPECIALIST"));
        
        List<Appointment> appointments;
        if (isAdmin) {
            appointments = appointmentService.getAllAppointments();
        } else if (isSpecialist) {
            appointments = appointmentService.getAppointmentsBySpecialistId(currentUserId);
        } else {
            appointments = appointmentService.getAppointmentsByClientId(currentUserId);
        }
        
        return ResponseEntity.ok(appointments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Integer id,
            @RequestBody UpdateAppointmentStatusRequest request,
            Authentication authentication) {
        Integer currentUserId = Integer.valueOf(authentication.getName());
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new NotFoundException("Appointment with id " + id + " not found"));
        
        Integer specialistId = appointment.getSpecialistOffering().getSpecialist().getId();
        if (!isAdmin && !currentUserId.equals(specialistId)) {
            throw new AccessDeniedException("Access denied");
        }
        
        Appointment updated = appointmentService.updateAppointmentStatus(id, request.getStatus());
        return ResponseEntity.ok(toResponse(updated));
    }
    
    private AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setClientId(appointment.getClient().getId());
        response.setClientName(appointment.getClient().getName());
        response.setSpecialistServiceId(appointment.getSpecialistOffering().getId());
        response.setSpecialistId(appointment.getSpecialistOffering().getSpecialist().getId());
        response.setSpecialistName(appointment.getSpecialistOffering().getSpecialist().getUser().getName());
        response.setServiceTitle(appointment.getSpecialistOffering().getServiceCatalog().getTitle());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setStatus(appointment.getStatus());
        return response;
    }
}
