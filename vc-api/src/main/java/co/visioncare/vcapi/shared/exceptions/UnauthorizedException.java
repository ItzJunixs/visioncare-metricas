package co.visioncare.vcapi.shared.exceptions;

/** Raised when the caller cannot be authenticated. Entry points translate it to HTTP 401. */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
