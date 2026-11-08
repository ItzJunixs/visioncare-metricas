package co.visioncare.vcapi.facturacion.internal.entity;

import java.time.Year;

public record InvoiceNumber(String value) {
    public static InvoiceNumber fromSequence(long sequence) {
        return new InvoiceNumber("FV-" + Year.now().getValue() + "-" + String.format("%04d", sequence));
    }

    @Override
    public String toString() {
        return value;
    }
}
