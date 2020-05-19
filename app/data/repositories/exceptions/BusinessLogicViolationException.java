package data.repositories.exceptions;

public class BusinessLogicViolationException extends RuntimeException {
    public BusinessLogicViolationException(String message) {
        super(message);
    }
}
