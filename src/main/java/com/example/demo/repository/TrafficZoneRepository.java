package com.example.demo.repository;

import com.example.demo.entity.TrafficZone;
import com.example.demo.enums.CongestionLevel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrafficZoneRepository extends JpaRepository<TrafficZone, Long> {

    List<TrafficZone> findByDistrict(String district);

    @Query("SELECT z FROM TrafficZone z WHERE z.currentCongestionLevel = com.example.demo.enums.CongestionLevel.HIGH OR z.currentCongestionLevel = com.example.demo.enums.CongestionLevel.CRITICAL")
    List<TrafficZone> findHighlyCongestedZones();

    @Query("SELECT z FROM TrafficZone z WHERE z.currentCongestionLevel = :level")
    List<TrafficZone> findByCongestionLevel(@Param("level") CongestionLevel level);
}
