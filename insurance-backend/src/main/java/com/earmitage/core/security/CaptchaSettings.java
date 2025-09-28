package com.earmitage.core.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "google.recaptcha.key")
public class CaptchaSettings {

    private boolean enabled;
    private String site;
    private String secret;
}
