package com.earmitage.hostme;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.earmitage.core.security.UserService;
import com.earmitage.core.security.dto.AuthenticationRequest;
import com.earmitage.core.security.dto.ContactType;
import com.earmitage.core.security.dto.RegistrationRequest;
import com.earmitage.core.security.dto.TokenDto;
import com.earmitage.core.security.repository.User;
import com.earmitage.core.security.repository.VerificationToken;
import com.earmitage.core.security.repository.VerificationTokenRepository;

import co.za.insurance.InsuranceApplication;
import co.za.insurance.Role;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {InsuranceApplication.class})
@Slf4j
public class RegistrationWorkflowTest {

    @Value("${local.server.port}")
    private int port;

    @Value("${app.url}")
    private String appUrl;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @BeforeEach
    public void setUp() {
        // Configure RestAssured to use the correct port for testing
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    public void testUserRegistration() {
        // Create a RegistrationRequest object with test data
        String username = "testuser";
        RegistrationRequest registrationRequest = new RegistrationRequest(username, // username
                "testpassword", // password
                "testpassword", // passwordConfirm
                "John", // firstname
                "Doe", // lastname
                "0005540735", // phone
                "testuser@example.com", // email
                "USER", // role
                LocalDate.of(1990, 1, 1), // dateOfBirth
                ContactType.EMAIL, // contactType,
                "88ewweewe",
                "+27"
        );
        registrationRequest.setRole(Role.ROLE_POLICY_HOLDER.name());

        given().contentType("application/json").accept("application/json").body(registrationRequest).when()
                .post(format("%s/unsecured/registrations/", appUrl)).then().statusCode(HttpStatus.CREATED.value())
                .body("message", equalTo("t***@****.com"));

        final VerificationToken verificationToken = tokenRepository.findByUserUsername(username);

        final TokenDto tokenConfirmation = new TokenDto(verificationToken.getToken(), registrationRequest.getUsername(),
                registrationRequest.getPassword(), registrationRequest.getIdNumber());

        given().contentType("application/json").accept("application/json").body(tokenConfirmation).when()
                .post(format("%s/unsecured/registration-confirmations/", appUrl)).then()
                .statusCode(HttpStatus.OK.value()).body("message", equalTo(UserService.TOKEN_VALID));

        final AuthenticationRequest authenticationRequest = new AuthenticationRequest(registrationRequest.getUsername(),
                registrationRequest.getPassword());
        User user = given().contentType("application/json").accept("application/json").body(authenticationRequest)
                .when().post(format("%s/unsecured/auth/", appUrl)).then().statusCode(HttpStatus.OK.value())
                .body("token", notNullValue()).extract().as(User.class);

        log.info(user.getToken());
    }
}
