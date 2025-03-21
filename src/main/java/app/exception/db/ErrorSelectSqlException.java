package app.exception.db;

public class ErrorSelectSqlException extends RuntimeException {

    public ErrorSelectSqlException(String message) {
        super(message);
    }

    public ErrorSelectSqlException(String message, Throwable cause) {
        super(message, cause);
    }
}

