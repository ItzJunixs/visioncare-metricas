package co.visioncare.vcapi.ordenes.internal.entity;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository {
    void append(UUID workOrderId, String previousStatus, String newStatus, String changedBy, String reason);

    List<OrderStatusHistoryEntry> findByWorkOrderId(UUID workOrderId);
}
