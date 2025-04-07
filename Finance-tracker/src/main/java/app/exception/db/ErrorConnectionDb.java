package app.exception.db;

public class ErrorConnectionDb extends RuntimeException {

    public ErrorConnectionDb(String message) {
        super(message);
    }

    public ErrorConnectionDb(String message, Throwable cause) {
        super(message, cause);
    }
}
