package com.earmitage.core.security.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.earmitage.core.security.repository.Subscription;
import com.earmitage.core.security.repository.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    private String uuid;
    private String username;
    private String firstname;
    private String lastname;
    private String phone;
    private String email;
    private String idNumber;
    private LocalDate dateOfBirth;
    private LocalDateTime dateOfJoining;
    private Boolean tos;
    private String language;
    private Boolean enabled;
    private LocalDateTime lastPasswordResetDate;
    private Set<String> roles;
    private String fcmToken;
    private String token;
    private ContactType contactType;
    private boolean isUsing2FA;
    private boolean locked;
    List<Subscription> subscriptions;

    public UserDto(final User entity) {
        this.username = entity.getUsername();
        this.firstname = entity.getFirstname();
        this.lastname = entity.getLastname();
        this.email = entity.getEmail();
        this.phone = entity.getPhone();
        this.dateOfBirth = entity.getDateOfBirth();
        this.uuid = entity.getUuid();
        this.idNumber = entity.getIdNumber();
        this.roles = entity.getRoles().stream().map(r ->r.getName()).collect(Collectors.toSet());
        this.dateOfBirth = entity.getDateOfBirth();
        this.dateOfJoining = entity.getDateCreated();
        this.tos = entity.getTos();
        this.isUsing2FA = entity.isUsing2FA();
        this.token = entity.getToken();
        this.contactType = entity.getContactType();
        this.enabled = entity.getEnabled();
        this.locked = entity.getLocked() == null? false:entity.getLocked();
        this.token = entity.getToken();

    }

    public String getFullName() {
        return getFirstname() + " " + getLastname();
    }
}
