package app.exception;

public class InvalidDataException  extends  IllegalArgumentException{

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
