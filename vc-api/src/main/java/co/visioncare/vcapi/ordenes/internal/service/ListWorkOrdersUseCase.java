package co.visioncare.vcapi.ordenes.internal.service;

import java.util.List;

import co.visioncare.vcapi.ordenes.internal.entity.WorkOrder;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderRepository;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderStatus;

public class ListWorkOrdersUseCase {

    private final WorkOrderRepository workOrders;

    public ListWorkOrdersUseCase(WorkOrderRepository workOrders) {
        this.workOrders = workOrders;
    }

    public List<WorkOrder> execute(WorkOrderStatus statusFilter) {
        return workOrders.findAll(statusFilter);
    }
}
