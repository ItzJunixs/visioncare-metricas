package co.visioncare.vcapi.facturacion.internal.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    long nextSequence();

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(UUID id);

    Optional<Invoice> findByWorkOrderId(UUID workOrderId);

    /** All invoices, newest first. Status is derived, so filtering by it happens in the use case. */
    List<Invoice> findAll();
}
