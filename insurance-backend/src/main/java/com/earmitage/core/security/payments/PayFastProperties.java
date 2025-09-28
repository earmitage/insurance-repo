package com.earmitage.core.security.payments;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "payfast")
@Getter
@Setter
public class PayFastProperties {

    private String merchantId;
    private String merchantKey;
    private String payfastUrl;
    private String secretKey;
    private String baseAppUrl;
    private String replyUrl;
    private List<String> domains;
    private String validationUrl;
}
