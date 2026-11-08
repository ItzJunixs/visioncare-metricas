package co.visioncare.vcapi.facturacion.internal.repository;

import java.util.List;

import co.visioncare.vcapi.facturacion.internal.entity.Invoice;
import co.visioncare.vcapi.facturacion.internal.entity.InvoiceNumber;
import co.visioncare.vcapi.facturacion.internal.entity.Payment;
import co.visioncare.vcapi.shared.money.Money;

/** Invoice and its payments are two tables, assembled by hand (no JPA relationship — see InvoiceData). */
final class InvoiceMapper {

    private InvoiceMapper() {
    }

    static InvoiceData toData(Invoice invoice) {
        return new InvoiceData(invoice.id(), invoice.number().value(), invoice.workOrderId(),
                invoice.subtotal().value(), invoice.discount().value(), invoice.tax().value(), invoice.createdAt());
    }

    static List<PaymentData> paymentsToData(Invoice invoice) {
        return invoice.payments().stream()
                .map(p -> new PaymentData(p.id(), p.invoiceId(), p.amount().value(), p.method(), p.paidAt()))
                .toList();
    }

    static Invoice toDomain(InvoiceData data, List<PaymentData> paymentRows) {
        List<Payment> payments = paymentRows.stream()
                .map(p -> new Payment(p.getId(), p.getInvoiceId(), Money.of(p.getAmount()), p.getMethod(), p.getPaidAt()))
                .toList();
        return Invoice.rehydrate(data.getId(), new InvoiceNumber(data.getNumber()), data.getWorkOrderId(),
                Money.of(data.getSubtotal()), Money.of(data.getDiscount()), Money.of(data.getTax()),
                data.getCreatedAt(), payments);
    }
}
