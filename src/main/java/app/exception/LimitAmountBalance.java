package app.exception;

public class LimitAmountBalance extends RuntimeException {
    public LimitAmountBalance(String message) {
        super(message);
    }

    public LimitAmountBalance(String message, Throwable cause) {
        super(message, cause);
    }
}
