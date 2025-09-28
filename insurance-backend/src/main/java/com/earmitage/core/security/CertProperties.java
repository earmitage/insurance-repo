package com.earmitage.core.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ConfigurationProperties("jwt")
@ToString
public class CertProperties {

    private String keyType;
    private String keyStoreFile;
    private String keyAlias;
    private String keyPassword;

}
