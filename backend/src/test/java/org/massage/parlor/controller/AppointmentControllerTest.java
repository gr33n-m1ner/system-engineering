package org.massage.parlor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.massage.parlor.dto.CreateAppointmentRequest;
import org.massage.parlor.dto.UpdateAppointmentStatusRequest;
import org.massage.parlor.model.*;
import org.massage.parlor.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerTest {

    private static final Integer CLIENT_ID = 1;
    private static final Integer SPECIALIST_ID = 2;
    private static final Integer SPECIALIST_SERVICE_ID = 1;
    private static final Integer APPOINTMENT_ID = 1;
    private static final LocalDateTime APPOINTMENT_TIME = LocalDateTime.of(2026, 3, 15, 10, 0);
    private static final String ROLE_CLIENT = "CLIENT";
    private static final String ROLE_SPECIALIST = "SPECIALIST";
    private static final String ROLE_ADMIN = "ADMIN";
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AppointmentService appointmentService;
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldAllowClientToCreateAppointment() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(SPECIALIST_SERVICE_ID, APPOINTMENT_TIME);
        Appointment appointment = createAppointment();
        
        when(appointmentService.createAppointment(eq(CLIENT_ID), any(CreateAppointmentRequest.class)))
                .thenReturn(appointment);
        
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(APPOINTMENT_ID))
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID));
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldAllowClientToGetOwnAppointments() throws Exception {
        Appointment appointment = createAppointment();
        
        when(appointmentService.getAppointmentsByClientId(CLIENT_ID)).thenReturn(List.of(appointment));
        
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(APPOINTMENT_ID))
                .andExpect(jsonPath("$[0].clientId").value(CLIENT_ID));
    }
    
    @Test
    @WithMockUser(username = "2", roles = ROLE_SPECIALIST)
    void shouldAllowSpecialistToGetOwnAppointments() throws Exception {
        Appointment appointment = createAppointment();
        
        when(appointmentService.getAppointmentsBySpecialistId(SPECIALIST_ID)).thenReturn(List.of(appointment));
        
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(APPOINTMENT_ID))
                .andExpect(jsonPath("$[0].specialistId").value(SPECIALIST_ID));
    }
    
    @Test
    @WithMockUser(username = "4", roles = ROLE_ADMIN)
    void shouldAllowAdminToGetAllAppointments() throws Exception {
        Appointment appointment = createAppointment();
        
        when(appointmentService.getAllAppointments()).thenReturn(List.of(appointment));
        
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(APPOINTMENT_ID));
    }
    
    @Test
    @WithMockUser(username = "2", roles = ROLE_SPECIALIST)
    void shouldAllowSpecialistToUpdateOwnAppointmentStatus() throws Exception {
        Appointment appointment = createAppointment();
        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.CONFIRMED);
        
        when(appointmentService.getAppointmentById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentService.updateAppointmentStatus(APPOINTMENT_ID, AppointmentStatus.CONFIRMED))
                .thenAnswer(invocation -> {
                    appointment.setStatus(AppointmentStatus.CONFIRMED);
                    return appointment;
                });
        
        mockMvc.perform(put("/api/appointments/" + APPOINTMENT_ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
    
    @Test
    @WithMockUser(username = "3", roles = ROLE_SPECIALIST)
    void shouldDenySpecialistToUpdateOtherSpecialistAppointmentStatus() throws Exception {
        Appointment appointment = createAppointment();
        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.CONFIRMED);
        
        when(appointmentService.getAppointmentById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        
        mockMvc.perform(put("/api/appointments/" + APPOINTMENT_ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "4", roles = ROLE_ADMIN)
    void shouldAllowAdminToUpdateAnyAppointmentStatus() throws Exception {
        Appointment appointment = createAppointment();
        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.CANCELLED);
        
        when(appointmentService.getAppointmentById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentService.updateAppointmentStatus(APPOINTMENT_ID, AppointmentStatus.CANCELLED))
                .thenAnswer(invocation -> {
                    appointment.setStatus(AppointmentStatus.CANCELLED);
                    return appointment;
                });
        
        mockMvc.perform(put("/api/appointments/" + APPOINTMENT_ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
    
    private User createUser(Integer id, String login, Role role) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        user.setRole(role);
        user.setName("Test User");
        return user;
    }
    
    private Appointment createAppointment() {
        User client = createUser(AppointmentControllerTest.CLIENT_ID, "client", Role.CLIENT);
        
        User specialistUser = createUser(AppointmentControllerTest.SPECIALIST_ID, "specialist", Role.SPECIALIST);
        Specialist specialist = new Specialist();
        specialist.setId(AppointmentControllerTest.SPECIALIST_ID);
        specialist.setUser(specialistUser);
        specialist.setExperience(5);
        specialist.setActive(true);

        ServiceCatalog serviceCatalog = new ServiceCatalog();
        serviceCatalog.setId(1);
        serviceCatalog.setTitle("Swedish Massage");
        serviceCatalog.setActive(true);

        SpecialistOffering specialistOffering = new SpecialistOffering();
        specialistOffering.setId(SPECIALIST_SERVICE_ID);
        specialistOffering.setSpecialist(specialist);
        specialistOffering.setServiceCatalog(serviceCatalog);
        specialistOffering.setPrice(new BigDecimal("50.00"));
        specialistOffering.setActive(true);

        Appointment appointment = new Appointment();
        appointment.setId(AppointmentControllerTest.APPOINTMENT_ID);
        appointment.setClient(client);
        appointment.setSpecialistOffering(specialistOffering);
        appointment.setAppointmentTime(APPOINTMENT_TIME);
        appointment.setStatus(AppointmentStatus.PENDING);
        
        return appointment;
    }
}
