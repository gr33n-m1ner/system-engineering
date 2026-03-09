package org.massage.parlor.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class RevenueStatisticsResponse {
    private BigDecimal totalRevenue;
    private Integer completedAppointments;
    private Map<Integer, SpecialistRevenue> revenueBySpecialist;
    
    @Data
    public static class SpecialistRevenue {
        private Integer specialistId;
        private String specialistName;
        private BigDecimal revenue;
        private Integer completedAppointments;
    }
}
