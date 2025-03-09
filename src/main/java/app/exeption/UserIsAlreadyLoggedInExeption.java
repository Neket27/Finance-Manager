package app.exeption;

public class UserIsAlreadyLoggedInExeption extends RuntimeException {
    public UserIsAlreadyLoggedInExeption(String message) {
        super(message);
    }

}
