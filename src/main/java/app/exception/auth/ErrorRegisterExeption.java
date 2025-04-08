package app.exception.auth;

public class ErrorRegisterExeption extends RuntimeException {

    public ErrorRegisterExeption(String message) {
        super(message);
    }

    public ErrorRegisterExeption(String message, Throwable cause) {
        super(message, cause);
    }

}
