package co.visioncare.vcapi.ordenes.internal.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrder;

public record WorkOrderResponse(UUID id, String orderCode, UUID patientId, UUID frameId, String status,
                                OffsetDateTime createdAt, OffsetDateTime actualDelivery) {
    public static WorkOrderResponse from(WorkOrder o) {
        return new WorkOrderResponse(o.id(), o.number().value(), o.patientId(), o.frameId(),
                o.status().name(), o.createdAt(), o.actualDelivery());
    }
}
