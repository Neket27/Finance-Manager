package app.exception.auth;

public class ErrorRegistrationException extends RuntimeException {

    public ErrorRegistrationException(String message) {
        super(message);
    }

    public ErrorRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

}
