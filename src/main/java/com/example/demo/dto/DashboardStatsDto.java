package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardStatsDto {

    private Long totalIncidents;
    private Long activeOutages;
    private Long congestedZones;
    private Long criticalAlerts;
}
