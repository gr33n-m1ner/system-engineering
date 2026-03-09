package org.massage.parlor.dto;

import lombok.Data;
import org.massage.parlor.model.AppointmentStatus;

import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    private Integer id;
    private Integer clientId;
    private String clientName;
    private Integer specialistServiceId;
    private Integer specialistId;
    private String specialistName;
    private String serviceTitle;
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;
}
