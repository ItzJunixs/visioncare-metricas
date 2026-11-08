package co.visioncare.vcapi.ordenes.internal.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

/** BR-11: every status change appends an entry — structural, not optional. */
public record OrderStatusHistoryEntry(UUID id, UUID workOrderId, String previousStatus, String newStatus,
                                      String changedBy, String reason, OffsetDateTime changedAt) {
}
