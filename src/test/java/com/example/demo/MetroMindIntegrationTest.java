package com.example.demo;

import com.example.demo.dto.AuthRequestDto;
import com.example.demo.dto.IncidentDto;
import com.example.demo.dto.OutageDto;
import com.example.demo.dto.RegisterDto;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetroMindIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CityUserRepository cityUserRepository;

    @Autowired
    private TrafficZoneRepository trafficZoneRepository;

    @Autowired
    private TrafficIncidentRepository trafficIncidentRepository;

    @Autowired
    private UtilityGridRepository utilityGridRepository;

    @Autowired
    private UtilityOutageRepository utilityOutageRepository;

    @Autowired
    private AlertNotificationRepository alertNotificationRepository;

    @BeforeEach
    void cleanDatabase() {
        trafficIncidentRepository.deleteAll();
        alertNotificationRepository.deleteAll();
        utilityOutageRepository.deleteAll();
        utilityGridRepository.deleteAll();
        trafficZoneRepository.deleteAll();
        cityUserRepository.deleteAll();
    }

    @Test
    void authEndpointsShouldRegisterAndLogin() throws Exception {
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto("admin1", "Password@123", Role.CITY_ADMINISTRATOR, "ADM-001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin1"))
                .andExpect(jsonPath("$.role").value("CITY_ADMINISTRATOR"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode registerNode = objectMapper.readTree(registerResponse);
        assertThat(registerNode.get("token").asText()).isNotBlank();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest("admin1", "Password@123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin1"))
                .andExpect(jsonPath("$.role").value("CITY_ADMINISTRATOR"));
    }

    @Test
    void incidentLifecycleAlertAndDashboardShouldWork() throws Exception {
        String adminToken = registerAndLogin("admin1", "Password@123", Role.CITY_ADMINISTRATOR, "ADM-001");
        String controllerToken = registerAndLogin("controller1", "Password@123", Role.TRAFFIC_CONTROLLER, "TRC-001");

        TrafficZone zone = trafficZoneRepository.save(TrafficZone.builder()
                .zoneName("Central Business District")
                .zoneCode("CBD-01")
                .district("Central")
                .currentCongestionLevel(CongestionLevel.HIGH)
                .signalCycleSeconds(90)
                .build());

        UtilityGrid grid = utilityGridRepository.save(UtilityGrid.builder()
                .gridName("Primary Power Grid")
                .gridType(GridType.POWER)
                .district("Central")
                .capacityUnits(100.0)
                .currentLoad(0.0)
                .status(GridStatus.OPERATIONAL)
                .build());

        mockMvc.perform(get("/api/incidents").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidentDto("Signal failure near central junction", "SIGNAL_FAILURE", "HIGH", zone.getZoneId(), "Traffic signal not responding"))))
                .andExpect(status().isCreated())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("TrafficIncident created successfully."));

        TrafficIncident incident = trafficIncidentRepository.findAll().get(0);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.REPORTED);
        assertThat(alertNotificationRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/incidents/" + incident.getIncidentId()).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentId").value(incident.getIncidentId()));

        mockMvc.perform(put("/api/incidents/" + incident.getIncidentId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidentDto("Updated incident title", "CONGESTION", "MEDIUM", zone.getZoneId(), "Updated description"))))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("TrafficIncident updated successfully."));

        mockMvc.perform(put("/api/incidents/" + incident.getIncidentId() + "/dispatch")
                        .header("Authorization", bearer(controllerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPATCHED"));

        mockMvc.perform(put("/api/incidents/" + incident.getIncidentId() + "/dispatch")
                        .header("Authorization", bearer(controllerToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only REPORTED incidents can be dispatched"));

        List<AlertNotification> alerts = alertNotificationRepository.findByTargetRole(Role.TRAFFIC_CONTROLLER);
        assertThat(alerts).hasSize(1);
        Long alertId = alerts.get(0).getAlertId();
        assertThat(alerts.get(0).isRead()).isFalse();

        mockMvc.perform(get("/api/alerts")
                        .header("Authorization", bearer(controllerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].targetRole").value("TRAFFIC_CONTROLLER"));

        mockMvc.perform(put("/api/alerts/" + alertId + "/read")
                        .header("Authorization", bearer(controllerToken)))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("Alert marked as read"));

        assertThat(alertNotificationRepository.findById(alertId)).get().extracting(AlertNotification::isRead).isEqualTo(true);

        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncidents").value(1))
                .andExpect(jsonPath("$.activeOutages").value(0))
                .andExpect(jsonPath("$.congestedZones").value(1))
                .andExpect(jsonPath("$.criticalAlerts").value(0));

        mockMvc.perform(delete("/api/incidents/" + incident.getIncidentId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("TrafficIncident deleted successfully."));

        mockMvc.perform(get("/api/incidents/" + incident.getIncidentId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("TrafficIncident not found"));
    }

    @Test
    void zoneGridAndSecurityControlsShouldWork() throws Exception {
        String adminToken = registerAndLogin("admin2", "Password@123", Role.CITY_ADMINISTRATOR, "ADM-002");
        String supervisorToken = registerAndLogin("supervisor1", "Password@123", Role.UTILITY_SUPERVISOR, "UTL-001");
        String controllerToken = registerAndLogin("controller2", "Password@123", Role.TRAFFIC_CONTROLLER, "TRC-002");

        TrafficZone congestedZone = trafficZoneRepository.save(TrafficZone.builder()
                .zoneName("Old Town")
                .zoneCode("OT-01")
                .district("North")
                .currentCongestionLevel(CongestionLevel.CRITICAL)
                .signalCycleSeconds(120)
                .build());

        TrafficZone incidentZone = trafficZoneRepository.save(TrafficZone.builder()
                .zoneName("Harbor Link")
                .zoneCode("HB-01")
                .district("North")
                .currentCongestionLevel(CongestionLevel.MODERATE)
                .signalCycleSeconds(75)
                .build());

        UtilityGrid grid = utilityGridRepository.save(UtilityGrid.builder()
                .gridName("Water Grid A")
                .gridType(GridType.WATER)
                .district("North")
                .capacityUnits(200.0)
                .currentLoad(50.0)
                .status(GridStatus.OPERATIONAL)
                .build());

        mockMvc.perform(get("/api/zones").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/zones/" + congestedZone.getZoneId()).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zoneCode").value("OT-01"));

        mockMvc.perform(get("/api/zones/congested").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/api/zones/" + congestedZone.getZoneId()).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("TrafficZone deleted successfully."));

        mockMvc.perform(get("/api/grids").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/grids/" + grid.getGridId()).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gridName").value("Water Grid A"));

        mockMvc.perform(put("/api/grids/" + grid.getGridId())
                        .header("Authorization", bearer(supervisorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newLoad\":190.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEGRADED"))
                .andExpect(jsonPath("$.currentLoad").value(190.0));

        UtilityGrid reloadedGrid = utilityGridRepository.findById(grid.getGridId()).orElseThrow();
        assertThat(reloadedGrid.getStatus()).isEqualTo(GridStatus.DEGRADED);

        mockMvc.perform(post("/api/grids/" + grid.getGridId() + "/outages")
                        .header("Authorization", bearer(supervisorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outageDto("POWER_FAILURE", "North Sector", "HIGH"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.outageType").value("POWER_FAILURE"));

        UtilityOutage outage = utilityOutageRepository.findAll().get(0);
        assertThat(outage.getStatus()).isEqualTo(OutageStatus.ACTIVE);
        assertThat(outage.getGrid().getGridId()).isEqualTo(grid.getGridId());

        mockMvc.perform(get("/api/alerts").header("Authorization", bearer(controllerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/dashboard/stats").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncidents").value(0))
                .andExpect(jsonPath("$.activeOutages").value(1))
                .andExpect(jsonPath("$.congestedZones").value(0))
                .andExpect(jsonPath("$.criticalAlerts").value(0));

        mockMvc.perform(get("/api/dashboard/stats").header("Authorization", bearer(supervisorToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", bearer(controllerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidentDto("Minor congestion", "CONGESTION", "LOW", incidentZone.getZoneId(), "Test"))))
                .andExpect(status().isCreated());
    }

    @Test
    void validationAndUnauthorizedPathsShouldFailCleanly() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\",\"fullName\":\"\",\"role\":\"\",\"district\":\"\",\"badgeNumber\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isUnauthorized());

        String adminToken = registerAndLogin("admin3", "Password@123", Role.CITY_ADMINISTRATOR, "ADM-003");
        String controllerToken = registerAndLogin("controller3", "Password@123", Role.TRAFFIC_CONTROLLER, "TRC-003");

        TrafficZone zone = trafficZoneRepository.save(TrafficZone.builder()
                .zoneName("Gateway")
                .zoneCode("GW-01")
                .district("South")
                .currentCongestionLevel(CongestionLevel.MODERATE)
                .signalCycleSeconds(75)
                .build());

        mockMvc.perform(delete("/api/zones/" + zone.getZoneId()).header("Authorization", bearer(controllerToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/incidents/999").header("Authorization", bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("TrafficIncident not found"));
    }

    private String registerAndLogin(String username, String password, Role role, String badgeNumber) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto(username, password, role, badgeNumber))))
                .andExpect(status().isOk());

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private RegisterDto registerDto(String username, String password, Role role, String badgeNumber) {
        RegisterDto dto = new RegisterDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setFullName(username + " full name");
        dto.setRole(role.name());
        dto.setDistrict("District");
        dto.setBadgeNumber(badgeNumber);
        return dto;
    }

    private AuthRequestDto authRequest(String username, String password) {
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    private IncidentDto incidentDto(String title, String incidentType, String severity, Long zoneId, String description) {
        IncidentDto dto = new IncidentDto();
        dto.setTitle(title);
        dto.setIncidentType(incidentType);
        dto.setSeverity(severity);
        dto.setZoneId(zoneId);
        dto.setDescription(description);
        return dto;
    }

    private OutageDto outageDto(String outageType, String affectedArea, String severity) {
        OutageDto dto = new OutageDto();
        dto.setOutageType(outageType);
        dto.setAffectedArea(affectedArea);
        dto.setSeverity(severity);
        dto.setEstimatedRestorationTime(java.time.LocalDateTime.now().plusHours(2));
        return dto;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
