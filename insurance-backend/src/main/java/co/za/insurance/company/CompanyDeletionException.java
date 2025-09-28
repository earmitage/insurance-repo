package co.za.insurance.company;

public class CompanyDeletionException extends RuntimeException {
    public CompanyDeletionException(String message) {
        super(message);
    }
}