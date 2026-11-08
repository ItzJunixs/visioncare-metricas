package co.visioncare.vcapi.ordenes.internal.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderRepository {
    long nextSequence();

    WorkOrder save(WorkOrder workOrder);

    Optional<WorkOrder> findById(UUID id);

    List<WorkOrder> findAll(WorkOrderStatus statusFilter);
}
