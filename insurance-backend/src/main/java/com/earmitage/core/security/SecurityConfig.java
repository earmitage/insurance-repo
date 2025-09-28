package com.earmitage.core.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties
public class SecurityConfig {

    private String jwtSecret;
    private Long jwtExpiration;
    private String jwtTokenHeader;
    private int maxPasswordRetries = 5;
    private boolean useCoolOffPeriod = true;
    private int minutesToLock = 1 * 60;
}
