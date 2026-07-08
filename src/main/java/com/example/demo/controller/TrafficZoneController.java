package com.example.demo.controller;

import com.example.demo.entity.TrafficZone;
import com.example.demo.service.TrafficZoneService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zones")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
public class TrafficZoneController {

    private final TrafficZoneService trafficZoneService;

    @GetMapping
    public ResponseEntity<List<TrafficZone>> getAllZones() {
        return ResponseEntity.ok(trafficZoneService.getAllZones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrafficZone> getZoneById(@PathVariable Long id) {
        return ResponseEntity.ok(trafficZoneService.getZoneById(id)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("TrafficZone not found")));
    }

    @GetMapping("/congested")
    public ResponseEntity<List<TrafficZone>> getHighlyCongestedZones() {
        return ResponseEntity.ok(trafficZoneService.getHighlyCongestedZones());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    public ResponseEntity<String> deleteZone(@PathVariable Long id) {
        trafficZoneService.deleteZone(id);
        return ResponseEntity.ok("TrafficZone deleted successfully.");
    }
}
