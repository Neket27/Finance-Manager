package app.exception.auth;

public class ErrorLoginExeption extends RuntimeException {
    public ErrorLoginExeption(String message) {
        super(message);
    }

    public ErrorLoginExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
