package com.example.demo.controller;

import com.example.demo.dto.OutageDto;
import com.example.demo.entity.UtilityGrid;
import com.example.demo.entity.UtilityOutage;
import com.example.demo.service.UtilityGridService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grids")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
public class UtilityGridController {

    private final UtilityGridService utilityGridService;

    @GetMapping
    public ResponseEntity<List<UtilityGrid>> getAllGrids() {
        return ResponseEntity.ok(utilityGridService.getAllGrids());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilityGrid> getGridById(@PathVariable Long id) {
        return ResponseEntity.ok(utilityGridService.getGridById(id)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("UtilityGrid not found")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('UTILITY_SUPERVISOR')")
    public ResponseEntity<UtilityGrid> updateLoad(@PathVariable Long id, @Valid @RequestBody UpdateLoadRequest request) {
        utilityGridService.updateLoad(id, request.getNewLoad());
        return ResponseEntity.ok(utilityGridService.getGridById(id)
                .orElseThrow(() -> new com.example.demo.exception.ResourceNotFoundException("UtilityGrid not found")));
    }

    @PostMapping("/{id}/outages")
    @PreAuthorize("hasRole('UTILITY_SUPERVISOR')")
    public ResponseEntity<UtilityOutage> registerOutage(@PathVariable Long id, @Valid @RequestBody OutageDto dto) {
        UtilityOutage outage = utilityGridService.registerOutage(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(outage);
    }

    public static class UpdateLoadRequest {
        @jakarta.validation.constraints.NotNull
        private Double newLoad;

        public Double getNewLoad() {
            return newLoad;
        }

        public void setNewLoad(Double newLoad) {
            this.newLoad = newLoad;
        }
    }
}
