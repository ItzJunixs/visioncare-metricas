package co.visioncare.vcapi.ordenes.internal.entity;

import java.util.Set;

/** BR-08: a work order only advances through this exact sequence; CANCELLED is the exception (BR-10). */
public enum WorkOrderStatus {
    QUOTE, APPROVED, IN_LAB, READY, DELIVERED, CANCELLED;

    private static final Set<WorkOrderStatus> CANCELLABLE_FROM = Set.of(QUOTE, APPROVED, IN_LAB, READY);

    public boolean canCancelFrom() {
        return CANCELLABLE_FROM.contains(this);
    }

    /** The one next status this status may advance to, or null when none (terminal, or must go via approve). */
    public WorkOrderStatus next() {
        return switch (this) {
            case APPROVED -> IN_LAB;
            case IN_LAB -> READY;
            case READY -> DELIVERED;
            default -> null;
        };
    }
}
