package co.visioncare.vcapi.ordenes.internal.service;

import java.util.UUID;

import co.visioncare.vcapi.inventario.InventarioFacade;
import co.visioncare.vcapi.ordenes.WorkOrderApprovedEvent;
import co.visioncare.vcapi.ordenes.internal.entity.OrderStatusHistoryRepository;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrder;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderRepository;
import co.visioncare.vcapi.shared.events.DomainEventPublisher;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;
import co.visioncare.vcapi.shared.tx.TransactionRunner;

/**
 * HU-07, BR-09: approve a work order and decrement the referenced frame's stock inside
 * the same transaction — two facade calls committing together, because this is one
 * deployable with one database (05-architecture/modular-monolith.md). The
 * {@link WorkOrderApprovedEvent} is published for `facturacion` to react to after commit.
 */
public class ApproveWorkOrderUseCase {

    private final WorkOrderRepository workOrders;
    private final OrderStatusHistoryRepository history;
    private final InventarioFacade inventario;
    private final TransactionRunner transaction;
    private final DomainEventPublisher events;

    public ApproveWorkOrderUseCase(WorkOrderRepository workOrders, OrderStatusHistoryRepository history,
                                   InventarioFacade inventario, TransactionRunner transaction,
                                   DomainEventPublisher events) {
        this.workOrders = workOrders;
        this.history = history;
        this.inventario = inventario;
        this.transaction = transaction;
        this.events = events;
    }

    public WorkOrder execute(UUID workOrderId, String changedBy) {
        return transaction.execute(() -> {
            WorkOrder order = workOrders.findById(workOrderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Work order not found: " + workOrderId));
            String previousStatus = order.status().name();

            order.approve();
            inventario.reserveStock(order.frameId(), 1, order.id()); // BR-06, BR-09
            WorkOrder saved = workOrders.save(order);
            history.append(order.id(), previousStatus, order.status().name(), changedBy, null); // BR-11

            var frame = inventario.findFrame(order.frameId()).orElseThrow();
            events.publish(new WorkOrderApprovedEvent(order.id(), order.number().value(), order.frameId(),
                    frame.sellPrice()));
            return saved;
        });
    }
}
