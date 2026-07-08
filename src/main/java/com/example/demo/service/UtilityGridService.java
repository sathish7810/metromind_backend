package com.example.demo.service;

import com.example.demo.dto.OutageDto;
import com.example.demo.entity.UtilityGrid;
import com.example.demo.entity.UtilityOutage;
import java.util.List;
import java.util.Optional;

public interface UtilityGridService {

    void updateLoad(Long id, Double newLoad);

    UtilityOutage registerOutage(Long gridId, OutageDto dto);

    List<UtilityGrid> getAllGrids();

    Optional<UtilityGrid> getGridById(Long id);
}
