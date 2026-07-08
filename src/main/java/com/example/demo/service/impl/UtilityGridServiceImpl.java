package com.example.demo.service.impl;

import com.example.demo.dto.OutageDto;
import com.example.demo.entity.UtilityGrid;
import com.example.demo.entity.UtilityOutage;
import com.example.demo.enums.GridStatus;
import com.example.demo.enums.OutageStatus;
import com.example.demo.enums.OutageType;
import com.example.demo.enums.Severity;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UtilityGridRepository;
import com.example.demo.repository.UtilityOutageRepository;
import com.example.demo.service.UtilityGridService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UtilityGridServiceImpl implements UtilityGridService {

    private final UtilityGridRepository utilityGridRepository;
    private final UtilityOutageRepository utilityOutageRepository;

    @Override
    public void updateLoad(Long id, Double newLoad) {
        UtilityGrid grid = utilityGridRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UtilityGrid not found"));
        if (grid.getCapacityUnits() == null || grid.getCapacityUnits() <= 0) {
            throw new BusinessValidationException("Grid capacity must be greater than zero");
        }
        grid.setCurrentLoad(newLoad);
        grid.setStatus(newLoad >= (grid.getCapacityUnits() * 0.9) ? GridStatus.DEGRADED : GridStatus.OPERATIONAL);
        utilityGridRepository.save(grid);
    }

    @Override
    public UtilityOutage registerOutage(Long gridId, OutageDto dto) {
        UtilityGrid grid = utilityGridRepository.findById(gridId)
                .orElseThrow(() -> new ResourceNotFoundException("UtilityGrid not found"));

        grid.setStatus(GridStatus.DEGRADED);
        utilityGridRepository.save(grid);

        UtilityOutage outage = UtilityOutage.builder()
                .grid(grid)
                .outageType(parseOutageType(dto.getOutageType()))
                .affectedArea(dto.getAffectedArea())
                .startTime(LocalDateTime.now())
                .status(OutageStatus.ACTIVE)
                .severity(parseSeverity(dto.getSeverity()))
                .estimatedRestorationTime(dto.getEstimatedRestorationTime())
                .build();
        return utilityOutageRepository.save(outage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilityGrid> getAllGrids() {
        return utilityGridRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UtilityGrid> getGridById(Long id) {
        return utilityGridRepository.findById(id);
    }

    private OutageType parseOutageType(String value) {
        try {
            return OutageType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BusinessValidationException("Invalid outage type");
        }
    }

    private Severity parseSeverity(String value) {
        try {
            return Severity.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BusinessValidationException("Invalid severity");
        }
    }
}
