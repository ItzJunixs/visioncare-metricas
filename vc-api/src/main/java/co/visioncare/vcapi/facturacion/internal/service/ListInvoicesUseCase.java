package co.visioncare.vcapi.facturacion.internal.service;

import java.util.List;

import co.visioncare.vcapi.facturacion.internal.entity.Invoice;
import co.visioncare.vcapi.facturacion.internal.entity.InvoiceRepository;

public class ListInvoicesUseCase {

    private final InvoiceRepository invoices;

    public ListInvoicesUseCase(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    public List<Invoice> execute(String statusFilter) {
        List<Invoice> all = invoices.findAll();
        if (statusFilter == null || statusFilter.isBlank()) {
            return all;
        }
        return all.stream().filter(i -> i.status().equalsIgnoreCase(statusFilter)).toList();
    }
}
