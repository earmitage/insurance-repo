package co.za.insurance.company;

public class CompanyValidationException extends RuntimeException {
    public CompanyValidationException(String message) {
        super(message);
    }
}