package com.earmitage.core.security.notifications;

public final class SMSIntegrationException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public SMSIntegrationException() {
        super();
    }

    public SMSIntegrationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SMSIntegrationException(final String message) {
        super(message);
    }

    public SMSIntegrationException(final Throwable cause) {
        super(cause);
    }
}
