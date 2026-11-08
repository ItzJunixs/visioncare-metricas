package co.visioncare.vcapi.facturacion.internal.entity.exceptions;

import co.visioncare.vcapi.shared.exceptions.ConflictException;

/** BR-13: the sum of an invoice's payments must never exceed its total. */
public class OverpaymentException extends ConflictException {
    public OverpaymentException(String invoiceNumber) {
        super("This payment would overpay invoice " + invoiceNumber);
    }
}
