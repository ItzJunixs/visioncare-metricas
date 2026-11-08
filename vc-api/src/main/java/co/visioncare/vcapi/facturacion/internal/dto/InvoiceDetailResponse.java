package co.visioncare.vcapi.facturacion.internal.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import co.visioncare.vcapi.facturacion.internal.entity.Invoice;

public record InvoiceDetailResponse(UUID id, String invoiceNumber, UUID workOrderId, BigDecimal subtotal,
                                    BigDecimal discount, BigDecimal tax, BigDecimal total, String status,
                                    BigDecimal balance, List<PaymentResponse> payments) {
    public static InvoiceDetailResponse from(Invoice i) {
        return new InvoiceDetailResponse(i.id(), i.number().value(), i.workOrderId(), i.subtotal().value(),
                i.discount().value(), i.tax().value(), i.total().value(), i.status(), i.balance().value(),
                i.payments().stream().map(PaymentResponse::from).toList());
    }
}
