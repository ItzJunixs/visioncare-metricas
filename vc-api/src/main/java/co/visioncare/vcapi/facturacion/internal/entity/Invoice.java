package co.visioncare.vcapi.facturacion.internal.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.visioncare.vcapi.facturacion.internal.entity.exceptions.OverpaymentException;
import co.visioncare.vcapi.shared.money.Money;

/**
 * Invoice aggregate root (HU-10). Created automatically when its work order is approved
 * (in-process event). BR-14: total is computed, never trusted from client input. BR-15:
 * status is derived from payments, never stored independently.
 */
public class Invoice {

    private final UUID id;
    private final InvoiceNumber number;
    private final UUID workOrderId;
    private final Money subtotal;
    private final Money discount;
    private final Money tax;
    private final Money total;
    private final OffsetDateTime createdAt;
    private final List<Payment> payments;

    private Invoice(UUID id, InvoiceNumber number, UUID workOrderId, Money subtotal, Money discount, Money tax,
                    OffsetDateTime createdAt, List<Payment> payments) {
        this.id = id;
        this.number = number;
        this.workOrderId = workOrderId;
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.total = subtotal.minus(discount).plus(tax); // BR-14
        this.createdAt = createdAt;
        this.payments = new ArrayList<>(payments);
    }

    public static Invoice issue(InvoiceNumber number, UUID workOrderId, Money subtotal) {
        return new Invoice(UUID.randomUUID(), number, workOrderId, subtotal, Money.ZERO, Money.ZERO,
                OffsetDateTime.now(), List.of());
    }

    public static Invoice rehydrate(UUID id, InvoiceNumber number, UUID workOrderId, Money subtotal, Money discount,
                                    Money tax, OffsetDateTime createdAt, List<Payment> payments) {
        return new Invoice(id, number, workOrderId, subtotal, discount, tax, createdAt, payments);
    }

    /** BR-13: a payment that would overpay the invoice is rejected. */
    public Payment registerPayment(Money amount, String method) {
        Money balance = balance();
        if (amount.isGreaterThan(balance)) {
            throw new OverpaymentException(number.value());
        }
        Payment payment = new Payment(UUID.randomUUID(), id, amount, method, OffsetDateTime.now());
        payments.add(payment);
        return payment;
    }

    public Money paid() {
        Money sum = Money.ZERO;
        for (Payment p : payments) {
            sum = sum.plus(p.amount());
        }
        return sum;
    }

    public Money balance() {
        return total.minus(paid());
    }

    /** BR-15: derived, never stored. */
    public String status() {
        Money paid = paid();
        if (paid.value().compareTo(total.value()) >= 0 && total.value().signum() > 0) {
            return "PAID";
        }
        if (paid.value().signum() > 0) {
            return "PARTIAL";
        }
        return "PENDING";
    }

    public UUID id() { return id; }
    public InvoiceNumber number() { return number; }
    public UUID workOrderId() { return workOrderId; }
    public Money subtotal() { return subtotal; }
    public Money discount() { return discount; }
    public Money tax() { return tax; }
    public Money total() { return total; }
    public OffsetDateTime createdAt() { return createdAt; }
    public List<Payment> payments() { return List.copyOf(payments); }
}
