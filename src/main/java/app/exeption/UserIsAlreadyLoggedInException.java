package app.exeption;

public class UserIsAlreadyLoggedInException extends RuntimeException {
    public UserIsAlreadyLoggedInException(String message) {
        super(message);
    }

}
