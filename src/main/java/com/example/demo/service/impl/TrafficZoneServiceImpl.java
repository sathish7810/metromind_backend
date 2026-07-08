package com.example.demo.service.impl;

import com.example.demo.entity.TrafficZone;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.TrafficZoneRepository;
import com.example.demo.service.TrafficZoneService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrafficZoneServiceImpl implements TrafficZoneService {

    private final TrafficZoneRepository trafficZoneRepository;

    @Override
    public List<TrafficZone> getAllZones() {
        return trafficZoneRepository.findAll();
    }

    @Override
    public Optional<TrafficZone> getZoneById(Long id) {
        return trafficZoneRepository.findById(id);
    }

    @Override
    public List<TrafficZone> getHighlyCongestedZones() {
        return trafficZoneRepository.findHighlyCongestedZones();
    }

    @Override
    @Transactional
    public void deleteZone(Long id) {
        TrafficZone zone = trafficZoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrafficZone not found"));
        trafficZoneRepository.delete(zone);
    }
}
