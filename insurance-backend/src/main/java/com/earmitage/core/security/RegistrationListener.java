package com.earmitage.core.security;

import static java.lang.String.format;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.earmitage.core.security.dto.NotificationType;
import com.earmitage.core.security.event.OnPasswordResetEvent;
import com.earmitage.core.security.event.OnPasswordResetVerifiedEvent;
import com.earmitage.core.security.event.OnRegistrationCompleteEvent;
import com.earmitage.core.security.notifications.AppProperties;
import com.earmitage.core.security.notifications.NotificationProperties;
import com.earmitage.core.security.notifications.NotificationsDto;
import com.earmitage.core.security.notifications.NotificationsService;
import com.earmitage.core.security.repository.PasswordResetToken;
import com.earmitage.core.security.repository.User;
import com.earmitage.core.security.repository.VerificationToken;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RegistrationListener {

    @Autowired
    private UserService service;

    @Autowired
    private AppProperties notificationProperties;

    @Autowired
    private NotificationsService notificationService;

    @EventListener
    public void onApplicationEvent(final OnRegistrationCompleteEvent event) {
        confirmRegistration(event);
    }

    @EventListener
    public void onApplicationEvent(final OnPasswordResetVerifiedEvent event) {
        // final User user = event.getUser();
        // createNotification("Account update",
        // NotificationProperties.NotificationContent.PASSWORD_RESET_SUCCESS.getEmail(),
        // null, user);
    }

    @EventListener
    public void onApplicationEvent(final OnPasswordResetEvent event) {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        final User user = event.getUser();
        final PasswordResetToken token = event.getToken();
        createNotification("Account update",
                format(NotificationProperties.NotificationContent.PASSWORD_RESET_TOKEN.getEmail(), token.getToken(),
                        formatter.format(token.getExpiryDate())),
                token.getExpiryDate(), user);
    }

    private void confirmRegistration(final OnRegistrationCompleteEvent event) {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        final User user = event.getUser();
        final Random random = new Random();
        final String token = String.format("%04d", random.nextInt(10000000) % 10000);
        final VerificationToken verificationToken = service.createVerificationTokenForUser(user, token);
        createNotification("Account update",
                format(NotificationProperties.NotificationContent.REGISTRATION_TOKEN.getEmail(),
                        notificationProperties.getNotifications().getNotificationsAppName(),
                        verificationToken.getToken(), formatter.format(verificationToken.getExpiryDate())),
                verificationToken.getExpiryDate(), user);
    }

    private void createNotification(final String subject, final String message, final Date expiryDate,
            final User user) {
        log.info("NOTIFICATION {}", message);
        LocalDateTime ldt = null;
        if (expiryDate != null) {
            final Instant current = expiryDate.toInstant();
            ldt = LocalDateTime.ofInstant(current, ZoneId.systemDefault());
        }
        final NotificationsDto notification = new NotificationsDto();
        notification.setAppName("APPName");
        notification.setExpiryDate(ldt);
        notification.setTitle(subject);
        notification.setMessage(message);
        notification.setType(NotificationType.valueOf(user.getContactType().name()));
        notification.setEmail(user.getEmail());
        notification.setPhoneNumber(user.getPhone());
        notification.setFcmToken(user.getFcmToken());
        notification.setRecipientName(user.getFirstname());
        notificationService.createNotification(notification);
    }

}
