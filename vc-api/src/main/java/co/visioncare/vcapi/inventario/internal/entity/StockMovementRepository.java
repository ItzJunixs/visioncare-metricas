package co.visioncare.vcapi.inventario.internal.entity;

import java.util.UUID;

/** BR-07: every stock quantity change is paired with a movement record. */
public interface StockMovementRepository {
    void record(UUID frameId, String type, int quantity, UUID workOrderId);

    boolean hasExitMovements(UUID frameId);

    void deleteByFrameId(UUID frameId);
}
