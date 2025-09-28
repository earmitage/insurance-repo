package com.earmitage.core.security.notifications;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.earmitage.core.security.repository.Notifications;

@RestController
@RequestMapping("/notifications")
public class NotificationsController {

    @Autowired
    NotificationsService notificationsService;

    @PostMapping(value = "/${app.url}/secured/notifications/{username}/save-notification")
    public NotificationsDto createNotification(@RequestHeader("Authorization") final String authorization,
            @PathVariable("username") final String username, @RequestBody final NotificationsDto notificationsDto) {
        notificationsDto.setUsername(username);
        return notificationsService.createNotification(notificationsDto);
    }

    @PostMapping(value = "/${app.url}/unsecured/notifications/sms/send/{msisdn}")
    public ResponseEntity<HttpStatus> sendSMS(@RequestBody final NotificationsDto notificationsDto,
            @PathVariable("msisdn") final String msisdn) {
        notificationsService.sendSMS(msisdn, notificationsDto.getMessage());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping(value = "/${app.url}/secured/notifications/{username}/get-notifications/{dateFrom}/{dateTo}")
    public List<Notifications> getNotification(@RequestHeader("Authorization") final String authorization,
            @PathVariable("username") final String username, @PathVariable("dateFrom") final LocalDateTime dateFrom,
            @PathVariable("dateTo") final LocalDateTime dateTo) {
        return notificationsService.getNotifications(username, dateFrom, dateTo);
    }
}
