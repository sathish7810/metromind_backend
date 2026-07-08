package com.example.demo.repository;

import com.example.demo.entity.UtilityOutage;
import com.example.demo.enums.OutageStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilityOutageRepository extends JpaRepository<UtilityOutage, Long> {

    List<UtilityOutage> findByStatus(OutageStatus status);

    List<UtilityOutage> findByGrid_GridId(Long gridId);

    long countByStatus(OutageStatus status);
}
