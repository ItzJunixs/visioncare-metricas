package co.visioncare.vcapi.shared.exceptions;

/** Raised when a requested aggregate does not exist. Entry points translate it to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
