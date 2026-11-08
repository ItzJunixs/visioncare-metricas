package co.visioncare.vcapi.ordenes;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Published after a work order's approval transaction commits. `facturacion` reacts to it
 * through an in-process, after-commit listener — it does not poll `ordenes`, and `ordenes`
 * does not wait on it (05-architecture/modular-monolith.md).
 */
public record WorkOrderApprovedEvent(UUID workOrderId, String orderCode, UUID frameId, BigDecimal frameSellPrice) {
}
