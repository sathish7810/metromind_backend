package com.example.demo.service;

import com.example.demo.entity.AlertNotification;
import com.example.demo.enums.Role;
import java.util.List;

public interface AlertNotificationService {

    List<AlertNotification> getAlertsForRole(Role role);

    void markAsRead(Long alertId);

    void markAsRead(Long alertId, Role role);

    List<AlertNotification> getUnreadAlerts(Role role);
}
