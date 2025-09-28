package com.earmitage.core.security.repository;

import java.time.LocalDateTime;

import com.earmitage.core.security.dto.NotificationType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Notifications extends BaseEntity {

    private String username;
    private String appName;
    private String message;

    private boolean readFlag;

    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String target;
}
