package app.exception;

public class ErrorLogoutException extends RuntimeException {

    public ErrorLogoutException(String message) {
        super(message);
    }

    public ErrorLogoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
