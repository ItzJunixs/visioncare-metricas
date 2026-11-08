package co.visioncare.vcapi.ordenes.internal.entity.exceptions;

import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;

/** BR-08: the requested status skips a stage in the defined sequence. */
public class InvalidTransitionException extends BusinessRuleException {
    public InvalidTransitionException(String from, String to) {
        super("Cannot move a work order from " + from + " to " + to);
    }
}
