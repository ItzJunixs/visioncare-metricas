package co.visioncare.vcapi.shared.exceptions;

/** Raised when a BR-NN domain invariant is violated. Entry points translate it to HTTP 422/409. */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
