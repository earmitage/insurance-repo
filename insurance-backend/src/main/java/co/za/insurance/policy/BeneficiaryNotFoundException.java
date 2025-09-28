package co.za.insurance.policy;

public final class BeneficiaryNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public BeneficiaryNotFoundException() {
        super();
    }

    public BeneficiaryNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BeneficiaryNotFoundException(final String message) {
        super(message);
    }

    public BeneficiaryNotFoundException(final Throwable cause) {
        super(cause);
    }

}
