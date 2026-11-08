package co.visioncare.vcapi.inventario;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the inventario module — the only entry point other modules may call.
 * `ordenes` calls {@link #reserveStock} inside its own transaction when approving a work
 * order (BR-09): the two facade calls commit together because both run against the same
 * database, in the same process — never a network hop.
 */
public interface InventarioFacade {

    Optional<FrameSnapshot> findFrame(UUID frameId);

    /** BR-06/BR-09: decrements stock by {@code quantity}; throws when that would go negative. */
    void reserveStock(UUID frameId, int quantity, UUID workOrderId);

    record FrameSnapshot(UUID id, String sku, String brand, String model, BigDecimal sellPrice, int stockQuantity) {
    }
}
