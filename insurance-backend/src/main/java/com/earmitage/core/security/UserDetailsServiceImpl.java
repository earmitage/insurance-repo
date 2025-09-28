package com.earmitage.core.security;

import static java.lang.String.format;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.earmitage.core.security.repository.User;
import com.earmitage.core.security.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfig securityConfig;

    @Override
    public UserDetails loadUserByUsername(final String username) {
        final Optional<User> user = userRepository.findByUsernameAndActiveTrueAndEnabledTrue(username);
        if (!user.isPresent()) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        }
        User foundUser = user.get();
        if (foundUser.getEnabled() != null && !foundUser.getEnabled()) {

            throw new UsernameNotFoundException(String.format("User '%s' not enabled.", username));
        }
        if (!foundUser.isActive()) {

            throw new UsernameNotFoundException(String.format("User '%s' not active.", username));
        }
        if (foundUser.getLocked() != null && foundUser.getLocked()) {
            if (securityConfig.isUseCoolOffPeriod()) {
                long minutesSinceLock = Duration.between(LocalDateTime.now(), foundUser.getLockedDate()).abs()
                        .getSeconds() * 60;
                if (minutesSinceLock >= securityConfig.getMinutesToLock()) {
                    return foundUser;
                }
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    format("Account %s is locked, contact your administrator", username));

        }
        return foundUser;
    }
}
