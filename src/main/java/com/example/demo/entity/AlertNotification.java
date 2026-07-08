package com.example.demo.entity;

import com.example.demo.enums.Role;
import com.example.demo.enums.Severity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "alert_notifications")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AlertNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role targetRole;

    @Column(nullable = false, length = 500)
    private String message;

    private String relatedEntityType;

    private Long relatedEntityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Builder.Default
    private boolean isRead = false;
}
