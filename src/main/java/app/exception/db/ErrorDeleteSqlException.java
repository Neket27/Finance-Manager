package app.exception.db;

public class ErrorDeleteSqlException extends RuntimeException {
    public ErrorDeleteSqlException(String message) {
        super(message);
    }

    public ErrorDeleteSqlException(String message, Throwable cause) {
        super(message, cause);
    }
}

