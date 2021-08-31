package hu.traileddevice.flashcard.exception;

public class QueriedDataDoesNotExistException extends RuntimeException {
    public QueriedDataDoesNotExistException(String message) {
        super(message);
    }
}
