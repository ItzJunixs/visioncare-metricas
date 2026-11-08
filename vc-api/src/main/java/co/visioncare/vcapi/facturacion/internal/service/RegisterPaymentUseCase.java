package co.visioncare.vcapi.facturacion.internal.service;

import java.math.BigDecimal;
import java.util.UUID;

import co.visioncare.vcapi.facturacion.internal.entity.Invoice;
import co.visioncare.vcapi.facturacion.internal.entity.InvoiceRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;
import co.visioncare.vcapi.shared.money.Money;

/** HU-10, BR-13: register a payment, rejecting one that would overpay the invoice. */
public class RegisterPaymentUseCase {

    private final InvoiceRepository invoices;

    public RegisterPaymentUseCase(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    public Invoice execute(UUID invoiceId, BigDecimal amount, String method) {
        Invoice invoice = invoices.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        invoice.registerPayment(Money.of(amount), method);
        return invoices.save(invoice);
    }
}
