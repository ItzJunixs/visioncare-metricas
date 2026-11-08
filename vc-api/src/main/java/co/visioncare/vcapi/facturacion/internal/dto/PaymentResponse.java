package co.visioncare.vcapi.facturacion.internal.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import co.visioncare.vcapi.facturacion.internal.entity.Payment;

public record PaymentResponse(UUID id, BigDecimal amount, String method, OffsetDateTime paidAt) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.id(), p.amount().value(), p.method(), p.paidAt());
    }
}
