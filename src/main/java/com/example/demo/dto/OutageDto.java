package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OutageDto {

    @NotBlank
    private String outageType;

    @NotBlank
    private String affectedArea;

    @NotBlank
    private String severity;

    @NotNull
    private LocalDateTime estimatedRestorationTime;
}
