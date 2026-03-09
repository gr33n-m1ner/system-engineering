package org.massage.parlor.controller;

import org.junit.jupiter.api.Test;
import org.massage.parlor.dto.RevenueStatisticsResponse;
import org.massage.parlor.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StatisticsControllerTest {
    
    private static final Integer SPECIALIST_ID = 2;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_SPECIALIST = "SPECIALIST";
    private static final String ROLE_CLIENT = "CLIENT";
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private StatisticsService statisticsService;
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_ADMIN)
    void shouldAllowAdminToGetRevenueStatistics() throws Exception {
        RevenueStatisticsResponse response = new RevenueStatisticsResponse();
        response.setTotalRevenue(new BigDecimal("500.00"));
        response.setCompletedAppointments(5);
        response.setRevenueBySpecialist(new HashMap<>());
        
        when(statisticsService.getRevenueStatistics(any(), any())).thenReturn(response);
        
        mockMvc.perform(get("/api/statistics/revenue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(500.00))
                .andExpect(jsonPath("$.completedAppointments").value(5));
    }
    
    @Test
    @WithMockUser(username = "3", roles = ROLE_CLIENT)
    void shouldDenyClientToGetRevenueStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "2", roles = ROLE_SPECIALIST)
    void shouldDenySpecialistToGetRevenueStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "2", roles = ROLE_SPECIALIST)
    void shouldAllowSpecialistToGetOwnRevenueStatistics() throws Exception {
        RevenueStatisticsResponse response = new RevenueStatisticsResponse();
        response.setTotalRevenue(new BigDecimal("250.00"));
        response.setCompletedAppointments(2);
        response.setRevenueBySpecialist(new HashMap<>());
        
        when(statisticsService.getRevenueBySpecialist(eq(SPECIALIST_ID), any(), any())).thenReturn(response);
        
        mockMvc.perform(get("/api/statistics/revenue/specialist/" + SPECIALIST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(250.00))
                .andExpect(jsonPath("$.completedAppointments").value(2));
    }
    
    @Test
    @WithMockUser(username = "3", roles = ROLE_SPECIALIST)
    void shouldDenySpecialistToGetOtherSpecialistRevenueStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue/specialist/" + SPECIALIST_ID))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_ADMIN)
    void shouldAllowAdminToGetAnySpecialistRevenueStatistics() throws Exception {
        RevenueStatisticsResponse response = new RevenueStatisticsResponse();
        response.setTotalRevenue(new BigDecimal("250.00"));
        response.setCompletedAppointments(2);
        response.setRevenueBySpecialist(new HashMap<>());
        
        when(statisticsService.getRevenueBySpecialist(eq(SPECIALIST_ID), any(), any())).thenReturn(response);
        
        mockMvc.perform(get("/api/statistics/revenue/specialist/" + SPECIALIST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(250.00))
                .andExpect(jsonPath("$.completedAppointments").value(2));
    }
}
