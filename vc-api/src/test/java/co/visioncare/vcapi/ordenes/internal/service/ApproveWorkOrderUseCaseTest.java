package co.visioncare.vcapi.ordenes.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.visioncare.vcapi.inventario.InventarioFacade;
import co.visioncare.vcapi.ordenes.WorkOrderApprovedEvent;
import co.visioncare.vcapi.ordenes.internal.entity.OrderStatusHistoryRepository;
import co.visioncare.vcapi.ordenes.internal.entity.PrescriptionSnapshot;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrder;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderNumber;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderRepository;
import co.visioncare.vcapi.ordenes.internal.entity.WorkOrderStatus;
import co.visioncare.vcapi.shared.events.DomainEventPublisher;
import co.visioncare.vcapi.shared.exceptions.ConflictException;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;
import co.visioncare.vcapi.shared.tx.TransactionRunner;

/** BR-09: approving an order decrements stock in the same transaction, or nothing happens. */
@ExtendWith(MockitoExtension.class)
class ApproveWorkOrderUseCaseTest {

    @Mock
    private WorkOrderRepository workOrders;
    @Mock
    private OrderStatusHistoryRepository history;
    @Mock
    private InventarioFacade inventario;
    @Mock
    private TransactionRunner transaction;
    @Mock
    private DomainEventPublisher events;

    private ApproveWorkOrderUseCase useCase;
    private UUID frameId;
    private WorkOrder order;

    @BeforeEach
    void setUp() {
        useCase = new ApproveWorkOrderUseCase(workOrders, history, inventario, transaction, events);
        frameId = UUID.randomUUID();
        order = WorkOrder.create(new WorkOrderNumber("OT-2026-0001"), UUID.randomUUID(), frameId,
                new PrescriptionSnapshot(-1.0, 0.0, 0, -1.0, 0.0, 0, null, null));
        lenient().when(transaction.execute(any())).thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
    }

    @Test
    void approvesTheQuoteDecrementsStockRecordsHistoryAndPublishesTheEvent() {
        when(workOrders.findById(order.id())).thenReturn(Optional.of(order));
        when(workOrders.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventario.findFrame(frameId)).thenReturn(Optional.of(
                new InventarioFacade.FrameSnapshot(frameId, "RB-1", "Ray-Ban", "Aviator", new BigDecimal("200000"), 4)));

        WorkOrder result = useCase.execute(order.id(), "seller");

        assertEquals(WorkOrderStatus.APPROVED, result.status());
        verify(inventario).reserveStock(frameId, 1, order.id());
        verify(history).append(order.id(), "QUOTE", "APPROVED", "seller", null);
        ArgumentCaptor<Object> published = ArgumentCaptor.forClass(Object.class);
        verify(events).publish(published.capture());
        WorkOrderApprovedEvent event = assertInstanceOf(WorkOrderApprovedEvent.class, published.getValue());
        assertEquals(order.id(), event.workOrderId());
        assertEquals(new BigDecimal("200000"), event.frameSellPrice());
    }

    @Test
    void leavesEverythingUntouchedWhenThereIsNotEnoughStock() {
        when(workOrders.findById(order.id())).thenReturn(Optional.of(order));
        doThrow(new ConflictException("Insufficient stock")).when(inventario).reserveStock(frameId, 1, order.id());

        assertThrows(ConflictException.class, () -> useCase.execute(order.id(), "seller"));

        verify(workOrders, never()).save(any(WorkOrder.class));
        verify(history, never()).append(any(), any(), any(), any(), any());
        verify(events, never()).publish(any());
    }

    @Test
    void reportsAnUnknownWorkOrder() {
        UUID id = UUID.randomUUID();
        when(workOrders.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(id, "seller"));

        verify(inventario, never()).reserveStock(any(), anyInt(), eq(id));
    }
}
