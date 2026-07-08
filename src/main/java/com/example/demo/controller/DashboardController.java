package com.example.demo.controller;

import com.example.demo.dto.DashboardStatsDto;
import com.example.demo.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
public class DashboardController {

    private final AnalyticsService analyticsService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    public ResponseEntity<DashboardStatsDto> getCityStats() {
        return ResponseEntity.ok(analyticsService.getCityStats());
    }
}
