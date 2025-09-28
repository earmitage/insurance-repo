package com.earmitage.core.security.notifications;

import static java.lang.String.format;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TwilioSender {

    @Autowired
    private AppProperties notificationProperties;

    public NotificationsDto createNotification(final NotificationsDto notificationsDto) {
        Twilio.init(notificationProperties.getNotifications().getTwilioAccountSid(),
                notificationProperties.getNotifications().getTwilioAuthToken());
        Message message = Message.creator(new PhoneNumber(format("%s", notificationsDto.getPhoneNumber())),
                new PhoneNumber(format("%s", notificationProperties.getNotifications().getTwilioSourceNumber())),
                notificationsDto.getMessage()).create();
        log.info("Message SID: {}", message.getSid());
        return notificationsDto;
    }
}
