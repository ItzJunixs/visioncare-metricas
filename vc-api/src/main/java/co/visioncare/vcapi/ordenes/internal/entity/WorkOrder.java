package co.visioncare.vcapi.ordenes.internal.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import co.visioncare.vcapi.ordenes.internal.entity.exceptions.CancelReasonRequiredException;
import co.visioncare.vcapi.ordenes.internal.entity.exceptions.InvalidTransitionException;

/**
 * Work order aggregate root (HU-08). Created in {@link WorkOrderStatus#QUOTE}; its prescription
 * is a frozen snapshot (BR-06 note in 06-data/models.md), never a live join to pacientes.
 */
public class WorkOrder {

    private final UUID id;
    private final WorkOrderNumber number;
    private final UUID patientId;
    private final UUID frameId;
    private final PrescriptionSnapshot prescriptionSnapshot;
    private WorkOrderStatus status;
    private final OffsetDateTime createdAt;
    private OffsetDateTime actualDelivery;

    private WorkOrder(UUID id, WorkOrderNumber number, UUID patientId, UUID frameId,
                      PrescriptionSnapshot prescriptionSnapshot, WorkOrderStatus status,
                      OffsetDateTime createdAt, OffsetDateTime actualDelivery) {
        this.id = id;
        this.number = number;
        this.patientId = patientId;
        this.frameId = frameId;
        this.prescriptionSnapshot = prescriptionSnapshot;
        this.status = status;
        this.createdAt = createdAt;
        this.actualDelivery = actualDelivery;
    }

    /** HU-08 · AC1: open a quote. */
    public static WorkOrder create(WorkOrderNumber number, UUID patientId, UUID frameId,
                                   PrescriptionSnapshot snapshot) {
        return new WorkOrder(UUID.randomUUID(), number, patientId, frameId, snapshot, WorkOrderStatus.QUOTE,
                OffsetDateTime.now(), null);
    }

    public static WorkOrder rehydrate(UUID id, WorkOrderNumber number, UUID patientId, UUID frameId,
                                      PrescriptionSnapshot snapshot, WorkOrderStatus status,
                                      OffsetDateTime createdAt, OffsetDateTime actualDelivery) {
        return new WorkOrder(id, number, patientId, frameId, snapshot, status, createdAt, actualDelivery);
    }

    /** HU-07: QUOTE -> APPROVED. Stock is decremented by the use case via InventarioFacade (BR-09). */
    public void approve() {
        requireTransition(WorkOrderStatus.QUOTE, WorkOrderStatus.APPROVED);
        this.status = WorkOrderStatus.APPROVED;
    }

    /** HU-09, BR-08: only the single next status in the sequence is accepted. */
    public void advanceTo(WorkOrderStatus target) {
        WorkOrderStatus expectedNext = status.next();
        if (expectedNext == null || expectedNext != target) {
            throw new InvalidTransitionException(status.name(), target.name());
        }
        this.status = target;
        if (target == WorkOrderStatus.DELIVERED) {
            this.actualDelivery = OffsetDateTime.now(); // BR-12
        }
    }

    /** BR-10: cancellable from any state before DELIVERED, with a reason. */
    public void cancel(String reason) {
        if (!status.canCancelFrom()) {
            throw new InvalidTransitionException(status.name(), WorkOrderStatus.CANCELLED.name());
        }
        if (reason == null || reason.isBlank()) {
            throw new CancelReasonRequiredException();
        }
        this.status = WorkOrderStatus.CANCELLED;
    }

    private void requireTransition(WorkOrderStatus from, WorkOrderStatus to) {
        if (status != from) {
            throw new InvalidTransitionException(status.name(), to.name());
        }
    }

    public UUID id() { return id; }
    public WorkOrderNumber number() { return number; }
    public UUID patientId() { return patientId; }
    public UUID frameId() { return frameId; }
    public PrescriptionSnapshot prescriptionSnapshot() { return prescriptionSnapshot; }
    public WorkOrderStatus status() { return status; }
    public OffsetDateTime createdAt() { return createdAt; }
    public OffsetDateTime actualDelivery() { return actualDelivery; }
}
