package com.earmitage.core.security.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    private String username;
    private String password;
    private String passwordConfirm;
    private String firstname;
    private String lastname;
    private String phone;
   
    private String email;
    private String role;
    private LocalDate dateOfBirth;
    private ContactType contactType;
    private String idNumber;
    private String countryCode;

    public static boolean allowed(final List<String> allowedDomains, final String email) {
        return allowedDomains.stream().filter(domain ->email.toLowerCase().endsWith(domain.toLowerCase())).findFirst()
                .isPresent();

    }

}
