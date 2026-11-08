package co.visioncare.vcapi.ordenes.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.visioncare.vcapi.ordenes.internal.entity.WorkOrder;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderRepository;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderStatus;

@Component
public class WorkOrderPersistenceAdapter implements WorkOrderRepository {

    private final WorkOrderJpaRepository jpa;

    public WorkOrderPersistenceAdapter(WorkOrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public long nextSequence() {
        return jpa.nextSequenceValue();
    }

    @Override
    public WorkOrder save(WorkOrder workOrder) {
        return WorkOrderMapper.toDomain(jpa.save(WorkOrderMapper.toData(workOrder)));
    }

    @Override
    public Optional<WorkOrder> findById(UUID id) {
        return jpa.findById(id).map(WorkOrderMapper::toDomain);
    }

    @Override
    public List<WorkOrder> findAll(WorkOrderStatus statusFilter) {
        List<WorkOrderData> rows = statusFilter == null
                ? jpa.findAllByOrderByCreatedAtDesc()
                : jpa.findAllByStatusOrderByCreatedAtDesc(statusFilter.name());
        return rows.stream().map(WorkOrderMapper::toDomain).toList();
    }
}
