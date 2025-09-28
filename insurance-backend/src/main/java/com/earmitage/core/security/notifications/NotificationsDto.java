package com.earmitage.core.security.notifications;

import java.time.LocalDateTime;

import com.earmitage.core.security.dto.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationsDto {

    private String title;
    private String username;
    private String appName;
    private String message;
    private String email;
    private String phoneNumber;
    private String fcmToken;
    private boolean read = false;
    private LocalDateTime expiryDate;
    private NotificationType type;

    private String recipientName;
}
