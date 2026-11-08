package co.visioncare.vcapi.facturacion.internal.service;

import java.math.BigDecimal;
import java.util.UUID;

import co.visioncare.vcapi.facturacion.internal.entity.Invoice;
import co.visioncare.vcapi.facturacion.internal.entity.InvoiceNumber;
import co.visioncare.vcapi.facturacion.internal.entity.InvoiceRepository;
import co.visioncare.vcapi.shared.money.Money;

/** Reacts to a work order being approved: issues its invoice. Subtotal = the frame's sell price. */
public class CreateInvoiceUseCase {

    private final InvoiceRepository invoices;

    public CreateInvoiceUseCase(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    public Invoice execute(UUID workOrderId, BigDecimal frameSellPrice) {
        if (invoices.findByWorkOrderId(workOrderId).isPresent()) {
            return invoices.findByWorkOrderId(workOrderId).get(); // idempotent: never double-invoice
        }
        Invoice invoice = Invoice.issue(InvoiceNumber.fromSequence(invoices.nextSequence()), workOrderId,
                Money.of(frameSellPrice));
        return invoices.save(invoice);
    }
}
