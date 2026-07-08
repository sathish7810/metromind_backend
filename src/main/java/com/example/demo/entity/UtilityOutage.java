package com.example.demo.entity;

import com.example.demo.enums.OutageStatus;
import com.example.demo.enums.OutageType;
import com.example.demo.enums.Severity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "utility_outages")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UtilityOutage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outageId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private UtilityGrid grid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutageType outageType;

    private String affectedArea;

    private LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    private OutageStatus status;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private LocalDateTime estimatedRestorationTime;
}
