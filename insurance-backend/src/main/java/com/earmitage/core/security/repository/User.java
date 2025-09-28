package com.earmitage.core.security.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.Size;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.earmitage.core.security.dto.ContactType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "USER_TYPE")
@NoArgsConstructor
@Table(name = "a_user")
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BaseEntity implements UserDetails {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true)
    private String username;

    @JsonIgnore
    @Size(min = 4)
    private String password;

    private String firstname;

    private String lastname;

    private String language;

    private String phone;

    @Column(unique = true)
    private String email;

    private Boolean enabled;
    private Boolean tos;
    
    private String idNumber;

    @Column(name = "LASTPASSWORDRESETDATE")
    private LocalDateTime lastPasswordResetDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    private LocalDate dateOfBirth;

    private String fcmToken;

    @Setter
    private boolean isUsing2FA;

    private String secret;

    @Transient
    private String token;

    private int passwordTries;
    private Boolean locked;
    private LocalDateTime lockedDate;
    
    @Enumerated(EnumType.STRING)
    private ContactType contactType; 

    public User(final String username, final String password, final String firstname, final String lastname,
            final String email, final String phone, final LocalDate dateOfBirth) {
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.enabled = false;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        passwordTries = 0;
    }

    public void resetPassword(final String newPassword) {
        password = BCrypt.hashpw(newPassword, BCrypt.gensalt());
    }

    @Override
    @JsonIgnore
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        if(roles == null) {
            return new ArrayList<>();
        }
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
