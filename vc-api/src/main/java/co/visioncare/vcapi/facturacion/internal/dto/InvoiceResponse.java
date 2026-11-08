package co.visioncare.vcapi.facturacion.internal.dto;

import java.math.BigDecimal;
import java.util.UUID;
import co.visioncare.vcapi.facturacion.internal.entity.Invoice;

public record InvoiceResponse(UUID id, String invoiceNumber, UUID workOrderId, BigDecimal subtotal,
                              BigDecimal discount, BigDecimal tax, BigDecimal total, String status) {
    public static InvoiceResponse from(Invoice i) {
        return new InvoiceResponse(i.id(), i.number().value(), i.workOrderId(), i.subtotal().value(),
                i.discount().value(), i.tax().value(), i.total().value(), i.status());
    }
}
