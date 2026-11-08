package co.visioncare.vcapi.inventario.internal.entity.exceptions;

import co.visioncare.vcapi.shared.exceptions.ConflictException;

/** BR-06/BR-09: not enough stock to approve the work order. */
public class InsufficientStockException extends ConflictException {
    public InsufficientStockException(String sku) {
        super("Insufficient stock for frame " + sku);
    }
}
