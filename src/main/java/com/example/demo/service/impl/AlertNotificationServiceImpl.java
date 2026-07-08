package com.example.demo.service.impl;

import com.example.demo.entity.AlertNotification;
import com.example.demo.enums.Role;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AlertNotificationRepository;
import com.example.demo.service.AlertNotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlertNotificationServiceImpl implements AlertNotificationService {

    private final AlertNotificationRepository alertNotificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlertNotification> getAlertsForRole(Role role) {
        return alertNotificationRepository.findByTargetRole(role);
    }

    @Override
    public void markAsRead(Long alertId) {
        markAsRead(alertId, null);
    }

    @Override
    public void markAsRead(Long alertId, Role role) {
        AlertNotification alertNotification = alertNotificationRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("AlertNotification not found"));
        if (role != null && alertNotification.getTargetRole() != role) {
            throw new BusinessValidationException("Alert does not belong to the authenticated role");
        }
        alertNotification.setRead(true);
        alertNotificationRepository.save(alertNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertNotification> getUnreadAlerts(Role role) {
        return alertNotificationRepository.findByTargetRoleAndIsReadFalse(role);
    }
}
