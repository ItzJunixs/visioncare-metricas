package co.visioncare.vcapi.inventario.internal.entity.exceptions;

import co.visioncare.vcapi.shared.exceptions.ConflictException;

public class DuplicateSkuException extends ConflictException {
    public DuplicateSkuException(String sku) {
        super("A frame with SKU " + sku + " already exists");
    }
}
