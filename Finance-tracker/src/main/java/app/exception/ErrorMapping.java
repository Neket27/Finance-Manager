package app.exception;

public class ErrorMapping extends IllegalArgumentException {
    public ErrorMapping(String message) {
        super(message);
    }

    public ErrorMapping(String message, Throwable cause) {
        super(message, cause);
    }
}
