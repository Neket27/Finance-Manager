package app.exception.db;

public class ErrorUpdateSqlException extends RuntimeException {
    public ErrorUpdateSqlException(String message) {
        super(message);
    }

    public ErrorUpdateSqlException(String message, Throwable cause) {
        super(message, cause);
    }
}
