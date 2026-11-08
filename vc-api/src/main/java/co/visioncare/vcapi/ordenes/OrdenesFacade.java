package co.visioncare.vcapi.ordenes;

import java.util.Optional;
import java.util.UUID;

/** Public API of the ordenes module — the only entry point other modules may call. */
public interface OrdenesFacade {
    Optional<WorkOrderSummary> findById(UUID id);

    record WorkOrderSummary(UUID id, String orderCode, UUID patientId, UUID frameId, String status) {
    }
}
