package co.visioncare.vcapi.facturacion.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.visioncare.vcapi.facturacion.internal.entity.Invoice;
import co.visioncare.vcapi.facturacion.internal.entity.InvoiceRepository;
import co.visioncare.vcapi.facturacion.internal.entity.Payment;

/**
 * Invoice and Payment are separate tables with no JPA relationship between them (InvoiceData
 * carries no collection field): this adapter loads each invoice's payments with a dedicated
 * query and assembles the domain aggregate by hand via Invoice.rehydrate.
 */
@Component
public class InvoicePersistenceAdapter implements InvoiceRepository {

    private final InvoiceJpaRepository invoiceJpa;
    private final PaymentJpaRepository paymentJpa;

    public InvoicePersistenceAdapter(InvoiceJpaRepository invoiceJpa, PaymentJpaRepository paymentJpa) {
        this.invoiceJpa = invoiceJpa;
        this.paymentJpa = paymentJpa;
    }

    @Override
    public long nextSequence() {
        return invoiceJpa.nextSequenceValue();
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceData saved = invoiceJpa.save(InvoiceMapper.toData(invoice));

        List<PaymentData> existing = paymentJpa.findAllByInvoiceIdOrderByPaidAtAsc(saved.getId());
        List<PaymentData> desired = InvoiceMapper.paymentsToData(invoice);
        List<UUID> desiredIds = desired.stream().map(PaymentData::getId).toList();
        for (Payment p : invoice.payments()) {
            if (existing.stream().noneMatch(e -> e.getId().equals(p.id()))) {
                paymentJpa.save(new PaymentData(p.id(), p.invoiceId(), p.amount().value(), p.method(), p.paidAt()));
            }
        }

        List<PaymentData> paymentRows = paymentJpa.findAllByInvoiceIdOrderByPaidAtAsc(saved.getId());
        return InvoiceMapper.toDomain(saved, paymentRows);
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return invoiceJpa.findById(id).map(this::hydrate);
    }

    @Override
    public Optional<Invoice> findByWorkOrderId(UUID workOrderId) {
        return invoiceJpa.findByWorkOrderId(workOrderId).map(this::hydrate);
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceJpa.findAllByOrderByCreatedAtDesc().stream().map(this::hydrate).toList();
    }

    private Invoice hydrate(InvoiceData data) {
        List<PaymentData> payments = paymentJpa.findAllByInvoiceIdOrderByPaidAtAsc(data.getId());
        return InvoiceMapper.toDomain(data, payments);
    }
}
