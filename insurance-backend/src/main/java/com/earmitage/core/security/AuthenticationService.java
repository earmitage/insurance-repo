package com.earmitage.core.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.earmitage.core.security.repository.Audit;
import com.earmitage.core.security.repository.AuditRepository;
import com.earmitage.core.security.repository.User;
import com.earmitage.core.security.repository.UserRepository;

import io.jsonwebtoken.Jwts;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    JwtEncoder encoder;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private UserRepository userRepository;

    public User authenticationToken(final String username, final String password) {
        try {

            final Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Reload password post-security so we can generate token
            // final UserDetails userDetails =
            // userDetailsService.loadUserByUsername(username);

            Optional<User> opt = userRepository.findByUsername(username);
            User user = null;
            if (opt.isPresent()) {
                user = opt.get();
                auditRepository.save(Audit.audit(user.getUuid(), username, null, "USER_LOGGED_IN", null));
            }
            if (user.getPasswordTries() != 0) {
                user.setPasswordTries(0);
                userRepository.save(user);
            }
            // user.setToken(token);

            if (user.getPasswordTries() != 0) {
                user.setPasswordTries(0);
                userRepository.save(user);
            }
            user.setToken(token(authentication));
            return user;
        } catch (final BadCredentialsException e) {
            // invalid login, update to user_attempts
            final Optional<User> byUsername = userRepository.findByUsername(username);
            byUsername.ifPresent(user -> {
                user.setPasswordTries(user.getPasswordTries() + 1);
                if (user.getPasswordTries() >= securityConfig.getMaxPasswordRetries()) {
                    user.setLocked(true);
                    user.setLockedDate(LocalDateTime.now());
                }

                userRepository.save(user);
            });
            throw e;

        }
    }

    public String token(Authentication authentication) {
        Instant now = Instant.now();
        //TODO
        long expiry = 36000L;
    // @formatter:off
		String scope = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(" "));
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("self")
				.issuedAt(now)
				.expiresAt(now.plusSeconds(expiry))
				.subject(authentication.getName())
				.claim("scope", scope)
				.build();
		// @formatter:on
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
    
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 hours
               
                .compact();
    }
}
