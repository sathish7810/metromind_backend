package com.example.demo.service;

import com.example.demo.entity.TrafficZone;
import java.util.List;
import java.util.Optional;

public interface TrafficZoneService {

    List<TrafficZone> getAllZones();

    Optional<TrafficZone> getZoneById(Long id);

    List<TrafficZone> getHighlyCongestedZones();

    void deleteZone(Long id);
}
