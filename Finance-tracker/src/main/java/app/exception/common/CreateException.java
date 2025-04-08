package app.exception.common;

public class CreateException extends RuntimeException {

    public CreateException(String message) {
        super(message);
    }

    public CreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
