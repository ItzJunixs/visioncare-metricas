package co.visioncare.vcapi.shared.exceptions;

/** Raised when a request conflicts with the resource's current state. Entry points translate it to HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
