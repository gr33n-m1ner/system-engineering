package org.massage.parlor.service;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.RevenueStatisticsResponse;
import org.massage.parlor.model.Appointment;
import org.massage.parlor.model.AppointmentStatus;
import org.massage.parlor.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final AppointmentRepository appointmentRepository;
    
    public RevenueStatisticsResponse getRevenueStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Appointment> completedAppointments = appointmentRepository
                .findByStatus(AppointmentStatus.COMPLETED)
                .stream()
                .filter(app -> isWithinPeriod(app.getAppointmentTime(), startDate, endDate))
                .toList();
        
        return calculateRevenue(completedAppointments);
    }
    
    public RevenueStatisticsResponse getRevenueBySpecialist(
            Integer specialistId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Appointment> completedAppointments = appointmentRepository
                .findByStatus(AppointmentStatus.COMPLETED)
                .stream()
                .filter(app -> app.getSpecialistOffering().getSpecialist().getId().equals(specialistId))
                .filter(app -> isWithinPeriod(app.getAppointmentTime(), startDate, endDate))
                .toList();
        
        return calculateRevenue(completedAppointments);
    }
    
    private RevenueStatisticsResponse calculateRevenue(List<Appointment> appointments) {
        RevenueStatisticsResponse response = new RevenueStatisticsResponse();
        Map<Integer, RevenueStatisticsResponse.SpecialistRevenue> revenueMap = new HashMap<>();
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (Appointment appointment : appointments) {
            Integer specialistId = appointment.getSpecialistOffering().getSpecialist().getId();
            String specialistName = appointment.getSpecialistOffering().getSpecialist().getUser().getName();
            BigDecimal price = appointment.getSpecialistOffering().getPrice();
            
            totalRevenue = totalRevenue.add(price);
            
            revenueMap.computeIfAbsent(specialistId, id -> {
                RevenueStatisticsResponse.SpecialistRevenue sr = 
                        new RevenueStatisticsResponse.SpecialistRevenue();
                sr.setSpecialistId(id);
                sr.setSpecialistName(specialistName);
                sr.setRevenue(BigDecimal.ZERO);
                sr.setCompletedAppointments(0);
                return sr;
            });
            
            RevenueStatisticsResponse.SpecialistRevenue sr = revenueMap.get(specialistId);
            sr.setRevenue(sr.getRevenue().add(price));
            sr.setCompletedAppointments(sr.getCompletedAppointments() + 1);
        }
        
        response.setTotalRevenue(totalRevenue);
        response.setCompletedAppointments(appointments.size());
        response.setRevenueBySpecialist(revenueMap);
        
        return response;
    }
    
    private boolean isWithinPeriod(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return (start == null || !dateTime.isBefore(start)) && (end == null || !dateTime.isAfter(end));
    }
}
