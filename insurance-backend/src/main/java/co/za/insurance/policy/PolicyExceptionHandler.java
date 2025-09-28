package co.za.insurance.policy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PolicyExceptionHandler {

    @ExceptionHandler(BeneficiaryNotFoundException.class)
    protected ResponseEntity<String> handleBadCredentialsException(final BadCredentialsException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Beneficiary not found");
    }
    
}
