package co.visioncare.vcapi.ordenes.internal.dto;

import java.time.OffsetDateTime;
import co.visioncare.vcapi.ordenes.internal.entity.OrderStatusHistoryEntry;

public record HistoryEntryResponse(String previousStatus, String newStatus, String changedBy, String reason,
                                   OffsetDateTime changedAt) {
    public static HistoryEntryResponse from(OrderStatusHistoryEntry e) {
        return new HistoryEntryResponse(e.previousStatus(), e.newStatus(), e.changedBy(), e.reason(), e.changedAt());
    }
}
