package data.repositories.exceptions;

public class ForbiddenExeption extends RuntimeException {
    public ForbiddenExeption(String message) {
        super(message);
    }
}
