package org.massage.parlor.service;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.CreateAppointmentRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.Appointment;
import org.massage.parlor.model.AppointmentStatus;
import org.massage.parlor.model.SpecialistOffering;
import org.massage.parlor.model.User;
import org.massage.parlor.repository.AppointmentRepository;
import org.massage.parlor.repository.SpecialistOfferingRepository;
import org.massage.parlor.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final SpecialistOfferingRepository specialistOfferingRepository;
    
    @Transactional
    public Appointment createAppointment(Integer clientId, CreateAppointmentRequest request) {
        User client = userRepository.getReferenceById(clientId);
        SpecialistOffering specialistOffering = specialistOfferingRepository.findByIdAndActiveTrue(
                request.getSpecialistServiceId())
                .orElseThrow(() -> new NotFoundException("Specialist offering with id " 
                        + request.getSpecialistServiceId() + " not found"));
        
        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setSpecialistOffering(specialistOffering);
        appointment.setAppointmentTime(request.getAppointmentTime());
        
        return appointmentRepository.save(appointment);
    }
    
    public List<Appointment> getAppointmentsByClientId(Integer clientId) {
        return appointmentRepository.findByClientId(clientId);
    }
    
    public List<Appointment> getAppointmentsBySpecialistId(Integer specialistId) {
        return appointmentRepository.findAll().stream()
                .filter(app -> app.getSpecialistOffering().getSpecialist().getId().equals(specialistId))
                .toList();
    }
    
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
    
    public Optional<Appointment> getAppointmentById(Integer id) {
        return appointmentRepository.findById(id);
    }
    
    @Transactional
    public Appointment updateAppointmentStatus(Integer id, AppointmentStatus status) {
        return appointmentRepository.findById(id)
                .map(appointment -> {
                    appointment.setStatus(status);
                    return appointmentRepository.save(appointment);
                })
                .orElseThrow(() -> new NotFoundException("Appointment with id " + id + " not found"));
    }
}
