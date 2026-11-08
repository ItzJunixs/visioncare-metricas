package co.visioncare.vcapi.ordenes.internal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.visioncare.vcapi.ordenes.OrdenesFacade;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrder;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderRepository;

@Component
public class OrdenesFacadeImpl implements OrdenesFacade {

    private final WorkOrderRepository workOrders;

    public OrdenesFacadeImpl(WorkOrderRepository workOrders) {
        this.workOrders = workOrders;
    }

    @Override
    public Optional<WorkOrderSummary> findById(UUID id) {
        return workOrders.findById(id).map(OrdenesFacadeImpl::toSummary);
    }

    private static WorkOrderSummary toSummary(WorkOrder o) {
        return new WorkOrderSummary(o.id(), o.number().value(), o.patientId(), o.frameId(), o.status().name());
    }
}
