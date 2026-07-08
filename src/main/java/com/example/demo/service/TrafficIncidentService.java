package com.example.demo.service;

import com.example.demo.dto.IncidentDto;
import com.example.demo.entity.TrafficIncident;
import java.util.List;

public interface TrafficIncidentService {

    TrafficIncident reportIncident(IncidentDto dto, String username);

    TrafficIncident dispatchResponse(Long id);

    List<TrafficIncident> getAllIncidents();

    TrafficIncident getIncidentById(Long id);

    TrafficIncident updateIncident(Long id, IncidentDto dto);

    void deleteIncident(Long id);
}
