package com.earmitage.core.security;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.earmitage.core.security.dto.RegistrationRequest;
import com.earmitage.core.security.dto.ResetPassword;
import com.earmitage.core.security.event.OnPasswordResetEvent;
import com.earmitage.core.security.event.OnRegistrationCompleteEvent;
import com.earmitage.core.security.repository.PasswordResetToken;
import com.earmitage.core.security.repository.PasswordResetTokenRepository;
import com.earmitage.core.security.repository.User;
import com.earmitage.core.security.repository.UserRepository;
import com.earmitage.core.security.repository.VerificationToken;
import com.earmitage.core.security.repository.VerificationTokenRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    
    @Autowired
    private CustomUserService customUserService;
    
    public static final String TOKEN_INVALID = "invalidToken";
    public static final String TOKEN_EXPIRED = "expired";
    public static final String TOKEN_VALID = "valid";

    public static String QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    public static String APP_NAME = "SpringRegistration";

    public User getUserByUsername(final String username) {
        return userRepository.findByUsernameAndActiveTrue(username);
    }

    public Optional<User> getUserByUsernameAndIsEnabled(final String username) {
        return userRepository.findByUsernameAndActiveTrueAndEnabledTrue(username);
    }

    public VerificationToken getVerificationToken(final String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

    public void save(final User user) {
        userRepository.save(user);
    }
    

    public User findByPhoneOrEmail(String phone, String email) {
        return userRepository.findByPhoneOrEmail(phone, email);
    }

    public void deleteUser(final User user) {
        final VerificationToken verificationToken = tokenRepository.findByUser(user);

        if (verificationToken != null) {
            tokenRepository.delete(verificationToken);
        }

        final PasswordResetToken passwordToken = passwordTokenRepository.findByUser(user);

        if (passwordToken != null) {
            passwordTokenRepository.delete(passwordToken);
        }

        userRepository.delete(user);
    }

    public VerificationToken createVerificationTokenForUser(final User user, final String token) {
        VerificationToken myToken = tokenRepository.findByUserUsername(user.getUsername());

        if (myToken == null) {
            myToken = new VerificationToken(token, user);
        } else {
            myToken.updateToken(token);
        }

        return tokenRepository.save(myToken);
    }

    public void generateNewVerificationToken(final String username) {
        final Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            // TODO eventPublisher.publishEvent(new
            // OnRegistrationCompleteEvent(user.get()));
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,String.format("User with username %s was not found", username));
    }

    public void createPasswordResetTokenForUser(final String username) {
        final Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {

            final Random random = new Random();
            final String token = String.format("%04d", random.nextInt(10000000) % 10000); // UUID.randomUUID().toString();

            PasswordResetToken myToken = passwordTokenRepository.findByUser(userOpt.get());
            if (myToken == null) {
                myToken = new PasswordResetToken(token, userOpt.get());
            } else {
                myToken.updateToken(token);
            }
            passwordTokenRepository.save(myToken);
            eventPublisher.publishEvent(new OnPasswordResetEvent(userOpt.get(), myToken));
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,String.format(
                "Account not found %s", username));
    }

    public PasswordResetToken getPasswordResetToken(final String token) {
        return passwordTokenRepository.findByToken(token);
    }

    public User getUserByPasswordResetToken(final String token) {
        return passwordTokenRepository.findByToken(token).getUser();
    }

    public Optional<User> getUserByID(final long id) {
        return userRepository.findById(id);
    }

    public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    public String validateVerificationToken(final String token, final String username) {
        final VerificationToken verificationToken = tokenRepository.findByTokenAndUserUsername(token, username);
        if (verificationToken == null) {
            return TOKEN_INVALID;
        }

        final User user = verificationToken.getUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            tokenRepository.delete(verificationToken);
            return TOKEN_EXPIRED;
        }

        user.setEnabled(true);
        userRepository.save(user);
        //eventPublisher.publishEvent(new OnUserVerifiedEvent(user));
        return TOKEN_VALID;
    }

    public String generateQRUrl(final User user) throws UnsupportedEncodingException {
        return QR_PREFIX + URLEncoder.encode(String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", APP_NAME,
                user.getEmail(), user.getSecret(), APP_NAME), "UTF-8");
    }

    public User updateUser2FA(final boolean use2FA) {
        final Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) curAuth.getPrincipal();
        currentUser.setUsing2FA(use2FA);
        currentUser = userRepository.save(currentUser);
        final Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, currentUser.getPassword(),
                curAuth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return currentUser;
    }

    public User registerNewUserAccount(final RegistrationRequest request) {
        try {
            User entity = new User(request.getUsername(), request.getPassword(), request.getFirstname(),
                    request.getLastname(), request.getEmail(), request.getPhone(), request.getDateOfBirth());
            entity.setIdNumber(request.getIdNumber());
            entity.setActive(true);
            entity.setEnabled(true);
            entity.setLocked(false);
            entity.setContactType(request.getContactType());
            customUserService.addRoles(entity, request);
            final User user = userRepository.save(entity);
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getRole(), request.getContactType()));
            return user;

        } catch (final Exception e) {
            log.error("Error registering user {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,String.format("Username %s is already taken", request.getUsername()));
        }

    }

    public User findUserByEmail(final String userEmail) {
        return userRepository.findByEmail(userEmail);
    }

    public String validatePasswordResetToken(final long id, final String token) {
        final PasswordResetToken passToken = passwordTokenRepository.findByToken(token);
        if ((passToken == null) || (passToken.getUser().getId() != id)) {
            return "invalidToken";
        }

        final Calendar cal = Calendar.getInstance();
        if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return "expired";
        }

        final User user = passToken.getUser();
        final Authentication auth = new UsernamePasswordAuthenticationToken(user, null,
                Arrays.asList(new SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        return null;
    }

    public void finalizePasswordReset(final ResetPassword resetPassword) {
        final PasswordResetToken token = passwordTokenRepository.findByTokenAndUserUsername(resetPassword.getToken(),
                resetPassword.getUsername());

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    String.format("A password reset attempt failed token used->[%s] for user->[%s]",
                            resetPassword.getToken(), resetPassword.getUsername()));
        }
        final User user = token.getUser();
        user.resetPassword(resetPassword.getNewPassword());
        userRepository.save(user);
        //eventPublisher.publishEvent(new OnPasswordResetVerifiedEvent(user));
    }

}
