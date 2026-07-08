package com.example.demo.controller;

import com.example.demo.entity.AlertNotification;
import com.example.demo.enums.Role;
import com.example.demo.service.AlertNotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
public class AlertNotificationController {

    private final AlertNotificationService alertNotificationService;

    @GetMapping
    public ResponseEntity<List<AlertNotification>> getAlerts(Authentication authentication) {
        Role role = currentRole(authentication);
        return ResponseEntity.ok(alertNotificationService.getAlertsForRole(role));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id, Authentication authentication) {
        Role role = currentRole(authentication);
        alertNotificationService.markAsRead(id, role);
        return ResponseEntity.ok("Alert marked as read");
    }

    private Role currentRole(Authentication authentication) {
        String authority = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Authenticated role not found"));
        String roleName = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
        return Role.valueOf(roleName);
    }
}
