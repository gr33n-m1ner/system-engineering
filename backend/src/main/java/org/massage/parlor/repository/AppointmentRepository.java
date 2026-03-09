package org.massage.parlor.repository;

import org.massage.parlor.model.Appointment;
import org.massage.parlor.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    
    List<Appointment> findByClientId(Integer clientId);
    
    List<Appointment> findByStatus(AppointmentStatus status);
}
