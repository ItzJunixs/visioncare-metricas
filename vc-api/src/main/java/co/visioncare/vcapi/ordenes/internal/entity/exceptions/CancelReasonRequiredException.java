package co.visioncare.vcapi.ordenes.internal.entity.exceptions;

import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;

/** BR-10: cancelling a work order requires a non-empty reason. */
public class CancelReasonRequiredException extends BusinessRuleException {
    public CancelReasonRequiredException() {
        super("A reason is required to cancel a work order");
    }
}
