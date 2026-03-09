package org.massage.parlor.controller;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.RevenueStatisticsResponse;
import org.massage.parlor.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RevenueStatisticsResponse> getRevenueStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime endDate) {
        RevenueStatisticsResponse statistics = statisticsService.getRevenueStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/revenue/specialist/{specialistId}")
    public ResponseEntity<RevenueStatisticsResponse> getRevenueBySpecialist(
            @PathVariable Integer specialistId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime endDate,
            Authentication authentication) {
        Integer currentUserId = Integer.valueOf(authentication.getName());
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !currentUserId.equals(specialistId)) {
            throw new AccessDeniedException("Access denied");
        }
        
        RevenueStatisticsResponse statistics = statisticsService.getRevenueBySpecialist(
                specialistId, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
}
