package com.example.demo.service.impl;

import com.example.demo.dto.IncidentDto;
import com.example.demo.entity.AlertNotification;
import com.example.demo.entity.CityUser;
import com.example.demo.entity.TrafficIncident;
import com.example.demo.entity.TrafficZone;
import com.example.demo.enums.IncidentStatus;
import com.example.demo.enums.IncidentType;
import com.example.demo.enums.Role;
import com.example.demo.enums.Severity;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AlertNotificationRepository;
import com.example.demo.repository.CityUserRepository;
import com.example.demo.repository.TrafficIncidentRepository;
import com.example.demo.repository.TrafficZoneRepository;
import com.example.demo.service.TrafficIncidentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TrafficIncidentServiceImpl implements TrafficIncidentService {

    private final TrafficIncidentRepository trafficIncidentRepository;
    private final TrafficZoneRepository trafficZoneRepository;
    private final CityUserRepository cityUserRepository;
    private final AlertNotificationRepository alertNotificationRepository;

    @Override
    public TrafficIncident reportIncident(IncidentDto dto, String username) {
        TrafficZone zone = trafficZoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("TrafficZone not found"));
        CityUser user = cityUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("CityUser not found"));

        TrafficIncident incident = TrafficIncident.builder()
                .title(dto.getTitle())
                .incidentType(parseIncidentType(dto.getIncidentType()))
                .severity(parseSeverity(dto.getSeverity()))
                .status(IncidentStatus.REPORTED)
                .zone(zone)
                .reportedBy(user)
                .reportedAt(LocalDateTime.now())
                .description(dto.getDescription())
                .build();

        TrafficIncident saved = trafficIncidentRepository.save(incident);

        if (saved.getSeverity() == Severity.HIGH || saved.getSeverity() == Severity.CRITICAL) {
            AlertNotification alertNotification = AlertNotification.builder()
                    .targetRole(Role.TRAFFIC_CONTROLLER)
                    .message("High severity incident reported: " + saved.getTitle())
                    .relatedEntityType("TrafficIncident")
                    .relatedEntityId(saved.getIncidentId())
                    .severity(saved.getSeverity())
                    .isRead(false)
                    .build();
            alertNotificationRepository.save(alertNotification);
        }

        return saved;
    }

    @Override
    public TrafficIncident dispatchResponse(Long id) {
        TrafficIncident incident = getIncidentById(id);
        if (incident.getStatus() != IncidentStatus.REPORTED) {
            throw new IllegalStateException("Only REPORTED incidents can be dispatched");
        }
        incident.setStatus(IncidentStatus.DISPATCHED);
        return trafficIncidentRepository.save(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrafficIncident> getAllIncidents() {
        return trafficIncidentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TrafficIncident getIncidentById(Long id) {
        return trafficIncidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrafficIncident not found"));
    }

    @Override
    public TrafficIncident updateIncident(Long id, IncidentDto dto) {
        TrafficIncident incident = getIncidentById(id);
        TrafficZone zone = trafficZoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("TrafficZone not found"));

        incident.setTitle(dto.getTitle());
        incident.setIncidentType(parseIncidentType(dto.getIncidentType()));
        incident.setSeverity(parseSeverity(dto.getSeverity()));
        incident.setZone(zone);
        incident.setDescription(dto.getDescription());
        return trafficIncidentRepository.save(incident);
    }

    @Override
    public void deleteIncident(Long id) {
        TrafficIncident incident = getIncidentById(id);
        trafficIncidentRepository.delete(incident);
    }

    private IncidentType parseIncidentType(String value) {
        try {
            return IncidentType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BusinessValidationException("Invalid incident type");
        }
    }

    private Severity parseSeverity(String value) {
        try {
            return Severity.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BusinessValidationException("Invalid severity");
        }
    }
}
