package com.earmitage.core.security.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpRequest {

    private LocalDateTime requestTime = LocalDateTime.now();
    private OtpType otpType;
    private String currentPassword;
}
