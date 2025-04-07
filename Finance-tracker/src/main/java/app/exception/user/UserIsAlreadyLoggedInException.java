package app.exception.user;

public class UserIsAlreadyLoggedInException extends RuntimeException {
    public UserIsAlreadyLoggedInException(String message) {
        super(message);
    }

}
