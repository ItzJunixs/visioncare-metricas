package co.visioncare.vcapi.ordenes.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.ordenes.internal.entity.OrderStatusHistoryRepository;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrder;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderRepository;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderStatus;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

/** HU-09, BR-08/BR-10/BR-11/BR-12: advance (or cancel) a work order's status. */
public class AdvanceWorkOrderStatusUseCase {

    private final WorkOrderRepository workOrders;
    private final OrderStatusHistoryRepository history;

    public AdvanceWorkOrderStatusUseCase(WorkOrderRepository workOrders, OrderStatusHistoryRepository history) {
        this.workOrders = workOrders;
        this.history = history;
    }

    public WorkOrder execute(UUID workOrderId, WorkOrderStatus target, String reason, String changedBy) {
        WorkOrder order = workOrders.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found: " + workOrderId));
        String previousStatus = order.status().name();

        if (target == WorkOrderStatus.CANCELLED) {
            order.cancel(reason);
        } else {
            order.advanceTo(target);
        }
        WorkOrder saved = workOrders.save(order);
        history.append(order.id(), previousStatus, order.status().name(), changedBy, reason); // BR-11
        return saved;
    }
}
