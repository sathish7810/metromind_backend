package com.example.demo.repository;

import com.example.demo.entity.AlertNotification;
import com.example.demo.enums.Role;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertNotificationRepository extends JpaRepository<AlertNotification, Long> {

    List<AlertNotification> findByTargetRole(Role role);

    List<AlertNotification> findByTargetRoleAndIsReadFalse(Role role);

    long countBySeverity(com.example.demo.enums.Severity severity);
}
