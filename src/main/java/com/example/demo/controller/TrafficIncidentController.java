package com.example.demo.controller;

import com.example.demo.dto.IncidentDto;
import com.example.demo.entity.TrafficIncident;
import com.example.demo.service.TrafficIncidentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
public class TrafficIncidentController {

    private final TrafficIncidentService trafficIncidentService;

    @GetMapping
    public ResponseEntity<List<TrafficIncident>> getAllIncidents() {
        return ResponseEntity.ok(trafficIncidentService.getAllIncidents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrafficIncident> getIncidentById(@PathVariable Long id) {
        return ResponseEntity.ok(trafficIncidentService.getIncidentById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CITY_ADMINISTRATOR', 'TRAFFIC_CONTROLLER')")
    public ResponseEntity<String> reportIncident(@Valid @RequestBody IncidentDto dto, Principal principal) {
        trafficIncidentService.reportIncident(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body("TrafficIncident created successfully.");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CITY_ADMINISTRATOR', 'TRAFFIC_CONTROLLER')")
    public ResponseEntity<String> updateIncident(@PathVariable Long id, @Valid @RequestBody IncidentDto dto) {
        trafficIncidentService.updateIncident(id, dto);
        return ResponseEntity.ok("TrafficIncident updated successfully.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    public ResponseEntity<String> deleteIncident(@PathVariable Long id) {
        trafficIncidentService.deleteIncident(id);
        return ResponseEntity.ok("TrafficIncident deleted successfully.");
    }

    @PutMapping("/{id}/dispatch")
    @PreAuthorize("hasRole('TRAFFIC_CONTROLLER')")
    public ResponseEntity<TrafficIncident> dispatchIncident(@PathVariable Long id) {
        return ResponseEntity.ok(trafficIncidentService.dispatchResponse(id));
    }
}
