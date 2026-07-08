package com.example.demo.config;

import com.example.demo.entity.AlertNotification;
import com.example.demo.entity.CityUser;
import com.example.demo.entity.TrafficIncident;
import com.example.demo.entity.TrafficZone;
import com.example.demo.entity.UtilityGrid;
import com.example.demo.entity.UtilityOutage;
import com.example.demo.enums.CongestionLevel;
import com.example.demo.enums.GridStatus;
import com.example.demo.enums.GridType;
import com.example.demo.enums.IncidentStatus;
import com.example.demo.enums.IncidentType;
import com.example.demo.enums.OutageStatus;
import com.example.demo.enums.OutageType;
import com.example.demo.enums.Role;
import com.example.demo.enums.Severity;
import com.example.demo.repository.AlertNotificationRepository;
import com.example.demo.repository.CityUserRepository;
import com.example.demo.repository.TrafficIncidentRepository;
import com.example.demo.repository.TrafficZoneRepository;
import com.example.demo.repository.UtilityGridRepository;
import com.example.demo.repository.UtilityOutageRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataSeeder {

    private final CityUserRepository cityUserRepository;
    private final TrafficZoneRepository trafficZoneRepository;
    private final TrafficIncidentRepository trafficIncidentRepository;
    private final UtilityGridRepository utilityGridRepository;
    private final UtilityOutageRepository utilityOutageRepository;
    private final AlertNotificationRepository alertNotificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedInitialData() {
        return args -> {
            CityUser admin = seedUser("admin1", "Password@123", Role.CITY_ADMINISTRATOR, "ADM-001", "City Administrator", "Central");
            CityUser controller = seedUser("controller1", "Password@123", Role.TRAFFIC_CONTROLLER, "TRC-001", "Traffic Controller", "Central");
            CityUser supervisor = seedUser("supervisor1", "Password@123", Role.UTILITY_SUPERVISOR, "UTL-001", "Utility Supervisor", "North");

            TrafficZone cbd = seedZone("Central Business District", "CBD-01", "Central", CongestionLevel.HIGH, 90);
            TrafficZone oldTown = seedZone("Old Town", "OT-01", "North", CongestionLevel.CRITICAL, 120);
            TrafficZone harbor = seedZone("Harbor Link", "HB-01", "South", CongestionLevel.MODERATE, 75);

            UtilityGrid powerGrid = seedGrid("Primary Power Grid", GridType.POWER, "Central", 100.0, 92.0, GridStatus.DEGRADED);
            UtilityGrid waterGrid = seedGrid("Water Grid A", GridType.WATER, "North", 200.0, 50.0, GridStatus.OPERATIONAL);
            UtilityGrid telecomGrid = seedGrid("Telecom Backbone", GridType.TELECOMMUNICATION, "South", 150.0, 30.0, GridStatus.OPERATIONAL);

            seedIncident("Signal failure near central junction", IncidentType.SIGNAL_FAILURE, Severity.HIGH,
                    IncidentStatus.REPORTED, cbd, controller, "Traffic signal not responding on the westbound lane.");
            seedIncident("Road block at old town bridge", IncidentType.ROAD_BLOCK, Severity.CRITICAL,
                    IncidentStatus.DISPATCHED, oldTown, admin, "Bridge access blocked by fallen debris.");
            seedIncident("Heavy congestion on harbor link", IncidentType.CONGESTION, Severity.MEDIUM,
                    IncidentStatus.REPORTED, harbor, controller, "Rush hour congestion building up.");

            seedOutage(powerGrid, OutageType.POWER_FAILURE, "Central District", Severity.HIGH, OutageStatus.ACTIVE,
                    LocalDateTime.now().plusHours(2));
            seedOutage(waterGrid, OutageType.WATER_DISRUPTION, "North Sector", Severity.MEDIUM, OutageStatus.RESOLVING,
                    LocalDateTime.now().plusHours(3));

            seedAlert(Role.TRAFFIC_CONTROLLER, "High severity incident reported: Signal failure near central junction",
                    "TrafficIncident", 1L, Severity.HIGH, false);
            seedAlert(Role.UTILITY_SUPERVISOR, "Power grid load above threshold in Central District",
                    "UtilityGrid", powerGrid.getGridId(), Severity.CRITICAL, false);
            seedAlert(Role.CITY_ADMINISTRATOR, "City dashboard requires review",
                    "Dashboard", 1L, Severity.MEDIUM, true);
        };
    }

    private CityUser seedUser(String username, String rawPassword, Role role, String badgeNumber, String fullName, String district) {
        return cityUserRepository.findByUsername(username).orElseGet(() ->
                cityUserRepository.save(CityUser.builder()
                        .username(username)
                        .passwordHash(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .fullName(fullName)
                        .district(district)
                        .badgeNumber(badgeNumber)
                        .isActive(true)
                        .build()));
    }

    private TrafficZone seedZone(String zoneName, String zoneCode, String district, CongestionLevel congestionLevel, Integer signalCycleSeconds) {
        return trafficZoneRepository.findByDistrict(district).stream()
                .filter(zone -> zoneCode.equals(zone.getZoneCode()))
                .findFirst()
                .orElseGet(() -> trafficZoneRepository.save(TrafficZone.builder()
                        .zoneName(zoneName)
                        .zoneCode(zoneCode)
                        .district(district)
                        .currentCongestionLevel(congestionLevel)
                        .signalCycleSeconds(signalCycleSeconds)
                        .build()));
    }

    private UtilityGrid seedGrid(String gridName, GridType gridType, String district, Double capacityUnits, Double currentLoad, GridStatus status) {
        return utilityGridRepository.findByDistrict(district).stream()
                .filter(grid -> gridName.equals(grid.getGridName()))
                .findFirst()
                .orElseGet(() -> utilityGridRepository.save(UtilityGrid.builder()
                        .gridName(gridName)
                        .gridType(gridType)
                        .district(district)
                        .capacityUnits(capacityUnits)
                        .currentLoad(currentLoad)
                        .status(status)
                        .build()));
    }

    private TrafficIncident seedIncident(String title,
                                         IncidentType incidentType,
                                         Severity severity,
                                         IncidentStatus status,
                                         TrafficZone zone,
                                         CityUser reportedBy,
                                         String description) {
        return trafficIncidentRepository.findAll().stream()
                .filter(incident -> title.equals(incident.getTitle()))
                .findFirst()
                .orElseGet(() -> trafficIncidentRepository.save(TrafficIncident.builder()
                        .title(title)
                        .incidentType(incidentType)
                        .severity(severity)
                        .status(status)
                        .zone(zone)
                        .reportedBy(reportedBy)
                        .reportedAt(LocalDateTime.now().minusHours(1))
                        .description(description)
                        .build()));
    }

    private UtilityOutage seedOutage(UtilityGrid grid,
                                     OutageType outageType,
                                     String affectedArea,
                                     Severity severity,
                                     OutageStatus status,
                                     LocalDateTime estimatedRestorationTime) {
        return utilityOutageRepository.findByGrid_GridId(grid.getGridId()).stream()
                .filter(outage -> outageType == outage.getOutageType())
                .findFirst()
                .orElseGet(() -> utilityOutageRepository.save(UtilityOutage.builder()
                        .grid(grid)
                        .outageType(outageType)
                        .affectedArea(affectedArea)
                        .startTime(LocalDateTime.now().minusHours(2))
                        .status(status)
                        .severity(severity)
                        .estimatedRestorationTime(estimatedRestorationTime)
                        .build()));
    }

    private AlertNotification seedAlert(Role targetRole,
                                        String message,
                                        String relatedEntityType,
                                        Long relatedEntityId,
                                        Severity severity,
                                        boolean isRead) {
        return alertNotificationRepository.findByTargetRole(targetRole).stream()
                .filter(alert -> message.equals(alert.getMessage()))
                .findFirst()
                .orElseGet(() -> alertNotificationRepository.save(AlertNotification.builder()
                        .targetRole(targetRole)
                        .message(message)
                        .relatedEntityType(relatedEntityType)
                        .relatedEntityId(relatedEntityId)
                        .severity(severity)
                        .isRead(isRead)
                        .build()));
    }
}
