package org.massage.parlor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.massage.parlor.model.AppointmentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentStatusRequest {
    private AppointmentStatus status;
}
