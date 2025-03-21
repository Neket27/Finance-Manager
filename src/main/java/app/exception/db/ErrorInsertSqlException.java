package app.exception.db;

public class ErrorInsertSqlException extends RuntimeException {
    public ErrorInsertSqlException(String message) {
        super(message);
    }

    public ErrorInsertSqlException(String message, Throwable cause) {
        super(message, cause);
    }
}
