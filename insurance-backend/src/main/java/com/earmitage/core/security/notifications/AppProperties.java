package com.earmitage.core.security.notifications;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("app")
@Getter
@Setter
public class AppProperties {
    
    private String insdeployAdmin;
    private String insdeployToken;
    private String insdeployContact;

    private NotificationProperties notifications;

}
