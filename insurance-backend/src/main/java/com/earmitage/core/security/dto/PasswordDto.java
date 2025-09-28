package com.earmitage.core.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordDto {

    private String oldPassword;
    private String newPassword;

}
