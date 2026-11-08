package co.visioncare.vcapi.ordenes.internal.service;

import java.util.List;
import java.util.UUID;

import co.visioncare.vcapi.ordenes.internal.entity.OrderStatusHistoryEntry;
import co.visioncare.vcapi.ordenes.internal.entity.OrderStatusHistoryRepository;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrder;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderRepository;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;

public class GetWorkOrderUseCase {

    private final WorkOrderRepository workOrders;
    private final OrderStatusHistoryRepository history;

    public GetWorkOrderUseCase(WorkOrderRepository workOrders, OrderStatusHistoryRepository history) {
        this.workOrders = workOrders;
        this.history = history;
    }

    public WorkOrder execute(UUID id) {
        return workOrders.findById(id).orElseThrow(() -> new ResourceNotFoundException("Work order not found: " + id));
    }

    public List<OrderStatusHistoryEntry> history(UUID id) {
        return history.findByWorkOrderId(id);
    }
}
