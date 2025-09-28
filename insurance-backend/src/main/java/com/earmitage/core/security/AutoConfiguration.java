package com.earmitage.core.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.earmitage.core.security.notifications.AppProperties;
import com.earmitage.core.security.payments.PayFastProperties;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@EnableConfigurationProperties({ CaptchaSettings.class, AppProperties.class , PayFastProperties.class})
public class AutoConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Insurance System API").version("1.0")
                .description("API documentation for the Booking System"));
    }
}
