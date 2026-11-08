package co.visioncare.vcapi.facturacion.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.facturacion.internal.entity.Invoice;
import co.visioncare.vcapi.facturacion.internal.entity.InvoiceRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

public class GetInvoiceUseCase {

    private final InvoiceRepository invoices;

    public GetInvoiceUseCase(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    public Invoice execute(UUID id) {
        return invoices.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }
}
