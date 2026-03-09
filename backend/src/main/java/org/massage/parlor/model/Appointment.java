package org.massage.parlor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"client_id", "specialist_service_id", "appointment_time"})
})
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_service_id")
    private SpecialistOffering specialistOffering;
    
    @Column(name = "appointment_time")
    private LocalDateTime appointmentTime;
    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.PENDING;
    
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = AppointmentStatus.PENDING;
        }
    }
}
