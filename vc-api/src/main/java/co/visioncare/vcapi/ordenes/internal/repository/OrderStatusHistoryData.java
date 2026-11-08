package co.visioncare.vcapi.ordenes.internal.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_status_history", schema = "ordenes")
public class OrderStatusHistoryData {

    @Id
    private UUID id;

    @Column(name = "work_order_id", nullable = false)
    private UUID workOrderId;

    @Column(name = "previous_status", nullable = false)
    private String previousStatus;

    @Column(name = "new_status", nullable = false)
    private String newStatus;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    private String reason;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    protected OrderStatusHistoryData() {
    }

    public OrderStatusHistoryData(UUID id, UUID workOrderId, String previousStatus, String newStatus,
                                  String changedBy, String reason) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.reason = reason;
        this.changedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getWorkOrderId() { return workOrderId; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getChangedBy() { return changedBy; }
    public String getReason() { return reason; }
    public OffsetDateTime getChangedAt() { return changedAt; }
}
