package co.visioncare.vcapi.facturacion.internal.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments", schema = "facturacion")
public class PaymentData {

    @Id
    private UUID id;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String method;

    @Column(name = "paid_at", nullable = false)
    private OffsetDateTime paidAt;

    protected PaymentData() {
    }

    public PaymentData(UUID id, UUID invoiceId, BigDecimal amount, String method, OffsetDateTime paidAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.method = method;
        this.paidAt = paidAt == null ? OffsetDateTime.now() : paidAt;
    }

    public UUID getId() { return id; }
    public UUID getInvoiceId() { return invoiceId; }
    public BigDecimal getAmount() { return amount; }
    public String getMethod() { return method; }
    public OffsetDateTime getPaidAt() { return paidAt; }
}
