package com.earmitage.core.security.notifications;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.earmitage.core.security.repository.Notifications;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationsService {

    @Autowired
    private TwilioSender whatsAppSender;

    @Autowired
    private AppProperties notificationProperties;

    public NotificationsDto createNotification(final NotificationsDto notificationsDto) {
        if (notificationProperties.getNotifications().isSmsEnabled()) {
            return whatsAppSender.createNotification(notificationsDto);
        }
        return null;
    }

    public List<Notifications> getNotifications(final String username, final LocalDateTime dateFrom,
            final LocalDateTime dateTo) {

        return null;
    }

    public void sendSMS(String msisdn, String message) {
        log.info("msisdn {} msisdn {}", message);

    }

}
