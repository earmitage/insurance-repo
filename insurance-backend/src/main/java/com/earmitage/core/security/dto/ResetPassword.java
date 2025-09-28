package com.earmitage.core.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPassword {

    private String username;
    private String newPassword;
    private String token;
}
