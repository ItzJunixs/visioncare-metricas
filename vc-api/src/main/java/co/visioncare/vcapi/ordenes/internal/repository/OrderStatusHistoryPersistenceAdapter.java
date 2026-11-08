package co.visioncare.vcapi.ordenes.internal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.visioncare.vcapi.ordenes.internal.entity.OrderStatusHistoryEntry;
import co.visioncare.vcapi.ordenes.internal.entity.OrderStatusHistoryRepository;

@Component
public class OrderStatusHistoryPersistenceAdapter implements OrderStatusHistoryRepository {

    private final OrderStatusHistoryJpaRepository jpa;

    public OrderStatusHistoryPersistenceAdapter(OrderStatusHistoryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void append(UUID workOrderId, String previousStatus, String newStatus, String changedBy, String reason) {
        jpa.save(new OrderStatusHistoryData(UUID.randomUUID(), workOrderId, previousStatus, newStatus, changedBy, reason));
    }

    @Override
    public List<OrderStatusHistoryEntry> findByWorkOrderId(UUID workOrderId) {
        return jpa.findAllByWorkOrderIdOrderByChangedAtAsc(workOrderId).stream()
                .map(d -> new OrderStatusHistoryEntry(d.getId(), d.getWorkOrderId(), d.getPreviousStatus(),
                        d.getNewStatus(), d.getChangedBy(), d.getReason(), d.getChangedAt()))
                .toList();
    }
}
