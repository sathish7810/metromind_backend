package com.example.demo.service.impl;

import com.example.demo.dto.DashboardStatsDto;
import com.example.demo.enums.OutageStatus;
import com.example.demo.enums.Severity;
import com.example.demo.repository.AlertNotificationRepository;
import com.example.demo.repository.TrafficIncidentRepository;
import com.example.demo.repository.TrafficZoneRepository;
import com.example.demo.repository.UtilityOutageRepository;
import com.example.demo.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TrafficIncidentRepository trafficIncidentRepository;
    private final UtilityOutageRepository utilityOutageRepository;
    private final TrafficZoneRepository trafficZoneRepository;
    private final AlertNotificationRepository alertNotificationRepository;

    @Override
    public DashboardStatsDto getCityStats() {
        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setTotalIncidents(trafficIncidentRepository.count());
        dto.setActiveOutages(utilityOutageRepository.countByStatus(OutageStatus.ACTIVE));
        dto.setCongestedZones((long) trafficZoneRepository.findHighlyCongestedZones().size());
        dto.setCriticalAlerts(alertNotificationRepository.countBySeverity(Severity.CRITICAL));
        return dto;
    }
}
