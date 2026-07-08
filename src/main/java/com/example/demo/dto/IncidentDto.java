package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IncidentDto {
    @NotBlank
    private String title;

    @NotBlank
    private String incidentType;

    @NotBlank
    private String severity;

    @NotNull
    private Long zoneId;

    @NotBlank
    private String description;
}
