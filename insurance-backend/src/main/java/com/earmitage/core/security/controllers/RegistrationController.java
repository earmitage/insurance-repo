package com.earmitage.core.security.controllers;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.earmitage.core.security.AuthenticationService;
import com.earmitage.core.security.CaptchaService;
import com.earmitage.core.security.CaptchaSettings;
import com.earmitage.core.security.UserService;
import com.earmitage.core.security.dto.ContactType;
import com.earmitage.core.security.dto.GenericResponse;
import com.earmitage.core.security.dto.Mask;
import com.earmitage.core.security.dto.RegistrationRequest;
import com.earmitage.core.security.dto.ResetPassword;
import com.earmitage.core.security.dto.TokenDto;
import com.earmitage.core.security.repository.User;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private CaptchaSettings captchaSettings;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping(value = "${app.url}/unsecured/registrations/", produces = {
            MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<GenericResponse> registrations(@RequestBody final RegistrationRequest request,
            final HttpServletRequest httpRequest) {
        if (captchaSettings.isEnabled()) {
            final String response = httpRequest.getParameter("g-recaptcha-response");
            captchaService.processResponse(response);
        }

        if (Strings.isBlank(request.getUsername())) {
            request.setUsername(request.getEmail());
        }
        if(request.getPhone().startsWith("0")) {
            request.setPhone(request.getPhone().substring(1));
        }
        request.setPhone(request.getCountryCode()+request.getPhone());

        User existing = userService.findByPhoneOrEmail(request.getPhone(), request.getEmail());

        if (existing != null) {
            if (existing.getPhone().equals(request.getPhone())) {
                //throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Phone is already registered");
                return new ResponseEntity<>(new GenericResponse("Phone is already registered", false), null, HttpStatus.BAD_REQUEST);

            }

            if (existing.getEmail().equals(request.getEmail())) {
                //throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Email is already registered");
                return new ResponseEntity<>(new GenericResponse("Email is already registered", false), null, HttpStatus.BAD_REQUEST);
            }
        }
        User registerNewUserAccount = userService.registerNewUserAccount(request);
        // TODO if simple workflow
        authenticationService.authenticationToken(request.getUsername(), request.getPassword());

        String masked = "";
        if (request.getContactType() == ContactType.SMS) {
            masked = Mask.cell(request.getPhone());
        }

        if (request.getContactType() == ContactType.EMAIL) {
            masked = Mask.email(request.getEmail());
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("MyResponseHeader", "MyValue");
        return new ResponseEntity<>(new GenericResponse(masked, true), responseHeaders, HttpStatus.CREATED);

    }

    @PostMapping(value = "${app.url}/unsecured/registration-confirmations/", produces = {
            MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity confirmRegistration(@RequestBody final TokenDto request) {
        final String result = userService.validateVerificationToken(request.getToken(), request.getUsername());
        if (result.equals(UserService.TOKEN_VALID)) {
            if (request.getPassword() != null) {
                final User user = authenticationService.authenticationToken(request.getUsername(),
                        request.getPassword());
                ResponseEntity.ok(user);
            }
            return ResponseEntity.ok(new GenericResponse(result, true));
        } else {
            return ResponseEntity.ok(new GenericResponse(result, false));
        }
    }

    // user activation - verification

    @PostMapping(value = "${app.url}/unsecured/resendRegistrationToken", produces = {
            MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public GenericResponse resendRegistrationToken(@RequestBody final TokenDto token) {
        userService.generateNewVerificationToken(token.getUsername());
        return new GenericResponse("Token successfully generated!", true);
    }

    // Reset password
    @PostMapping(value = "${app.url}/unsecured/resetPassword/", produces = {
            MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public GenericResponse resetPassword(@RequestBody final TokenDto token) {
        userService.createPasswordResetTokenForUser(token.getUsername());
        return new GenericResponse("success", true);

    }

    @PostMapping(value = "${app.url}/users/update/2fa", produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = {
            MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public GenericResponse modifyUser2FA(@RequestParam("use2FA") final boolean use2FA)
            throws UnsupportedEncodingException {
        final User user = userService.updateUser2FA(use2FA);
        if (use2FA) {
            return new GenericResponse(userService.generateQRUrl(user), true);
        }
        return null;
    }

    @PostMapping(value = "${app.url}/unsecured/users/password-reset-finalize/", produces = {
            MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public GenericResponse finalizePasswordReset(@RequestBody final ResetPassword request) {
        userService.finalizePasswordReset(request);
        return new GenericResponse("success", true);
    }

    // ============== NON-API ============

    private String getAppUrl(final HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    public void authWithAuthManager(final HttpServletRequest request, final String username, final String password) {
        final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username,
                password);
        authToken.setDetails(new WebAuthenticationDetails(request));
        final Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        // SecurityContextHolder.getContext());
    }

    public void authWithoutPassword(final User user) {
        // user.getAuthorities().stream().map(role -> role.getPrivileges()).flatMap(list
        // ->
        // list.stream()).distinct().collect(Collectors.toList());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        final Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
