package com.example.demo.entity;

import com.example.demo.enums.CongestionLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "traffic_zones")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TrafficZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long zoneId;

    @Column(nullable = false)
    private String zoneName;

    @Column(unique = true, nullable = false)
    private String zoneCode;

    private String district;

    @Enumerated(EnumType.STRING)
    private CongestionLevel currentCongestionLevel;

    private Integer signalCycleSeconds;
}
