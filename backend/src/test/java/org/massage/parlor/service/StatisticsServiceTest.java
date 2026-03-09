package org.massage.parlor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.massage.parlor.dto.RevenueStatisticsResponse;
import org.massage.parlor.model.*;
import org.massage.parlor.repository.AppointmentRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @InjectMocks
    private StatisticsService statisticsService;
    
    private static final Integer SPECIALIST_ID_1 = 1;
    private static final Integer SPECIALIST_ID_2 = 2;
    private static final BigDecimal PRICE_1 = new BigDecimal("100.00");
    private static final BigDecimal PRICE_2 = new BigDecimal("150.00");
    private static final LocalDateTime APPOINTMENT_TIME_1 = LocalDateTime.of(2026, 3, 10, 10, 0);
    private static final LocalDateTime APPOINTMENT_TIME_2 = LocalDateTime.of(2026, 3, 11, 14, 0);
    
    @Test
    void shouldCalculateTotalRevenue() {
        List<Appointment> completedAppointments = List.of(
                createAppointment(1, SPECIALIST_ID_1, PRICE_1, APPOINTMENT_TIME_1),
                createAppointment(2, SPECIALIST_ID_1, PRICE_2, APPOINTMENT_TIME_2),
                createAppointment(3, SPECIALIST_ID_2, PRICE_1, APPOINTMENT_TIME_1)
        );
        
        when(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED))
                .thenReturn(completedAppointments);
        
        RevenueStatisticsResponse result = statisticsService.getRevenueStatistics(null, null);
        
        assertNotNull(result);
        assertEquals(new BigDecimal("350.00"), result.getTotalRevenue());
        assertEquals(3, result.getCompletedAppointments());
        assertEquals(2, result.getRevenueBySpecialist().size());
    }
    
    @Test
    void shouldCalculateRevenueWithinPeriod() {
        List<Appointment> completedAppointments = List.of(
                createAppointment(1, SPECIALIST_ID_1, PRICE_1, APPOINTMENT_TIME_1),
                createAppointment(2, SPECIALIST_ID_1, PRICE_2, APPOINTMENT_TIME_2),
                createAppointment(3, SPECIALIST_ID_2, PRICE_1, 
                        LocalDateTime.of(2026, 3, 12, 10, 0))
        );
        
        when(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED))
                .thenReturn(completedAppointments);
        
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 11, 23, 59);
        
        RevenueStatisticsResponse result = statisticsService.getRevenueStatistics(start, end);
        
        assertNotNull(result);
        assertEquals(new BigDecimal("250.00"), result.getTotalRevenue());
        assertEquals(2, result.getCompletedAppointments());
    }
    
    @Test
    void shouldCalculateRevenueBySpecialist() {
        List<Appointment> completedAppointments = List.of(
                createAppointment(1, SPECIALIST_ID_1, PRICE_1, APPOINTMENT_TIME_1),
                createAppointment(2, SPECIALIST_ID_1, PRICE_2, APPOINTMENT_TIME_2),
                createAppointment(3, SPECIALIST_ID_2, PRICE_1, APPOINTMENT_TIME_1)
        );
        
        when(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED))
                .thenReturn(completedAppointments);
        
        RevenueStatisticsResponse result = statisticsService.getRevenueBySpecialist(SPECIALIST_ID_1, null, null);
        
        assertNotNull(result);
        assertEquals(new BigDecimal("250.00"), result.getTotalRevenue());
        assertEquals(2, result.getCompletedAppointments());
        assertEquals(1, result.getRevenueBySpecialist().size());
        
        RevenueStatisticsResponse.SpecialistRevenue specialistRevenue = 
                result.getRevenueBySpecialist().get(SPECIALIST_ID_1);
        assertNotNull(specialistRevenue);
        assertEquals(new BigDecimal("250.00"), specialistRevenue.getRevenue());
        assertEquals(2, specialistRevenue.getCompletedAppointments());
    }
    
    @Test
    void shouldReturnZeroRevenueWhenNoCompletedAppointments() {
        when(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED)).thenReturn(List.of());
        
        RevenueStatisticsResponse result = statisticsService.getRevenueStatistics(null, null);
        
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
        assertEquals(0, result.getCompletedAppointments());
        assertTrue(result.getRevenueBySpecialist().isEmpty());
    }
    
    private Appointment createAppointment(Integer id, Integer specialistId, BigDecimal price, 
                                         LocalDateTime appointmentTime) {
        User specialistUser = new User();
        specialistUser.setId(specialistId);
        specialistUser.setName("Specialist " + specialistId);
        
        Specialist specialist = new Specialist();
        specialist.setId(specialistId);
        specialist.setUser(specialistUser);
        
        ServiceCatalog serviceCatalog = new ServiceCatalog();
        serviceCatalog.setId(1);
        serviceCatalog.setTitle("Massage");
        
        SpecialistOffering specialistOffering = new SpecialistOffering();
        specialistOffering.setId(id);
        specialistOffering.setSpecialist(specialist);
        specialistOffering.setServiceCatalog(serviceCatalog);
        specialistOffering.setPrice(price);
        
        User client = new User();
        client.setId(100);
        client.setName("Client");
        
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setClient(client);
        appointment.setSpecialistOffering(specialistOffering);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        
        return appointment;
    }
}
