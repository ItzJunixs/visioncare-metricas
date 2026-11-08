package co.visioncare.vcapi.facturacion.internal.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import co.visioncare.vcapi.shared.money.Money;

public record Payment(UUID id, UUID invoiceId, Money amount, String method, OffsetDateTime paidAt) {
}
