package co.visioncare.vcapi.facturacion.internal.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Payments are a separate table, loaded and assembled by the persistence adapter (not a JPA relationship). */
@Entity
@Table(name = "invoices", schema = "facturacion")
public class InvoiceData {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String number;

    @Column(name = "work_order_id", nullable = false, unique = true)
    private UUID workOrderId;

    @Column(nullable = false)
    private BigDecimal subtotal;
    @Column(nullable = false)
    private BigDecimal discount;
    @Column(nullable = false)
    private BigDecimal tax;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected InvoiceData() {
    }

    public InvoiceData(UUID id, String number, UUID workOrderId, BigDecimal subtotal, BigDecimal discount,
                       BigDecimal tax, OffsetDateTime createdAt) {
        this.id = id;
        this.number = number;
        this.workOrderId = workOrderId;
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.createdAt = createdAt == null ? OffsetDateTime.now() : createdAt;
    }

    public UUID getId() { return id; }
    public String getNumber() { return number; }
    public UUID getWorkOrderId() { return workOrderId; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTax() { return tax; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
