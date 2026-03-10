package org.massage.parlor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.massage.parlor.dto.CreateAppointmentRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.*;
import org.massage.parlor.repository.AppointmentRepository;
import org.massage.parlor.repository.SpecialistOfferingRepository;
import org.massage.parlor.repository.UserRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SpecialistOfferingRepository specialistOfferingRepository;
    
    @InjectMocks
    private AppointmentService appointmentService;
    
    private static final Integer CLIENT_ID = 1;
    private static final Integer SPECIALIST_ID = 2;
    private static final Integer SPECIALIST_SERVICE_ID = 1;
    private static final Integer APPOINTMENT_ID = 1;
    private static final LocalDateTime APPOINTMENT_TIME = LocalDateTime.of(2026, 3, 15, 10, 0);
    
    @Test
    void shouldCreateAppointment() {
        User client = new User();
        client.setId(CLIENT_ID);
        
        SpecialistOffering specialistOffering = new SpecialistOffering();
        specialistOffering.setId(SPECIALIST_SERVICE_ID);
        specialistOffering.setActive(true);
        
        CreateAppointmentRequest request = new CreateAppointmentRequest(SPECIALIST_SERVICE_ID, APPOINTMENT_TIME);
        
        when(userRepository.getReferenceById(CLIENT_ID)).thenReturn(client);
        when(specialistOfferingRepository.findByIdAndActiveTrue(SPECIALIST_SERVICE_ID))
                .thenReturn(Optional.of(specialistOffering));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Appointment result = appointmentService.createAppointment(CLIENT_ID, request);
        
        assertNotNull(result);
        assertEquals(APPOINTMENT_TIME, result.getAppointmentTime());
        verify(appointmentRepository).save(any(Appointment.class));
    }
    
    @Test
    void shouldGetAppointmentsBySpecialistId() {
        User specialistUser = new User();
        specialistUser.setId(SPECIALIST_ID);
        
        Specialist specialist = new Specialist();
        specialist.setId(SPECIALIST_ID);
        specialist.setUser(specialistUser);
        
        SpecialistOffering specialistOffering = new SpecialistOffering();
        specialistOffering.setId(SPECIALIST_SERVICE_ID);
        specialistOffering.setSpecialist(specialist);
        
        Appointment appointment = new Appointment();
        appointment.setId(APPOINTMENT_ID);
        appointment.setSpecialistOffering(specialistOffering);
        
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment));
        
        List<Appointment> result = appointmentService.getAppointmentsBySpecialistId(SPECIALIST_ID);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(APPOINTMENT_ID, result.get(0).getId());
    }
    
    @Test
    void shouldUpdateAppointmentStatus() {
        Appointment appointment = new Appointment();
        appointment.setId(APPOINTMENT_ID);
        appointment.setStatus(AppointmentStatus.PENDING);
        
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Appointment result = appointmentService.updateAppointmentStatus(APPOINTMENT_ID, AppointmentStatus.CONFIRMED);
        
        assertNotNull(result);
        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());
        verify(appointmentRepository).save(appointment);
    }
    
    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentAppointment() {
        when(appointmentRepository.findById(999)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, 
                () -> appointmentService.updateAppointmentStatus(999, AppointmentStatus.CONFIRMED));
    }
    
    @Test
    void shouldThrowExceptionWhenCreatingAppointmentForNonexistentClient() {
        User client = new User();
        SpecialistOffering specialistOffering = new SpecialistOffering();
        specialistOffering.setActive(true);
        CreateAppointmentRequest request = new CreateAppointmentRequest(SPECIALIST_SERVICE_ID, APPOINTMENT_TIME);
        
        when(userRepository.getReferenceById(CLIENT_ID)).thenReturn(client);
        when(specialistOfferingRepository.findByIdAndActiveTrue(SPECIALIST_SERVICE_ID))
                .thenReturn(Optional.of(specialistOffering));
        when(appointmentRepository.save(any(Appointment.class))).thenThrow(new DataIntegrityViolationException(""));
        
        assertThrows(DataIntegrityViolationException.class, 
                () -> appointmentService.createAppointment(CLIENT_ID, request));
    }
    
    @Test
    void shouldThrowExceptionWhenCreatingAppointmentForNonexistentService() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(SPECIALIST_SERVICE_ID, APPOINTMENT_TIME);
        
        when(specialistOfferingRepository.findByIdAndActiveTrue(SPECIALIST_SERVICE_ID)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, 
                () -> appointmentService.createAppointment(CLIENT_ID, request));
    }
    
    @Test
    void shouldCancelAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(APPOINTMENT_ID);
        appointment.setStatus(AppointmentStatus.PENDING);
        
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Appointment result = appointmentService.cancelAppointment(APPOINTMENT_ID);
        
        assertNotNull(result);
        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
        verify(appointmentRepository).save(appointment);
    }
    
    @Test
    void shouldThrowExceptionWhenCancellingCompletedAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(APPOINTMENT_ID);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        
        assertThrows(IllegalStateException.class, 
                () -> appointmentService.cancelAppointment(APPOINTMENT_ID));
    }
}
