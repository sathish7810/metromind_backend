package com.example.demo.entity;

import com.example.demo.enums.GridStatus;
import com.example.demo.enums.GridType;
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
@Table(name = "utility_grids")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UtilityGrid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gridId;

    @Column(nullable = false)
    private String gridName;

    @Enumerated(EnumType.STRING)
    private GridType gridType;

    private String district;

    @Column(nullable = false)
    private Double capacityUnits;

    @Builder.Default
    private Double currentLoad = 0.0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GridStatus status = GridStatus.OPERATIONAL;
}
